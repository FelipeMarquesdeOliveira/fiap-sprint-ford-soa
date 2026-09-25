package br.com.ford.vinshare.service;

import br.com.ford.vinshare.domain.autenticacao.DadosLogin;
import br.com.ford.vinshare.domain.autenticacao.DadosTokenJWT;
import br.com.ford.vinshare.domain.autenticacao.RefreshToken;
import br.com.ford.vinshare.domain.autenticacao.RefreshTokenRepository;
import br.com.ford.vinshare.domain.usuario.Usuario;
import br.com.ford.vinshare.domain.usuario.UsuarioRepository;
import br.com.ford.vinshare.infra.exception.CodigoErro;
import br.com.ford.vinshare.infra.security.JwtProperties;
import br.com.ford.vinshare.infra.security.TokenInvalidoException;
import br.com.ford.vinshare.infra.security.TokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;

/**
 * Fluxo de autenticação:
 * <ol>
 *   <li><b>login</b>: valida e-mail/senha (BCrypt) e emite access token (JWT, 15 min) + refresh token (opaco, 7 dias);</li>
 *   <li><b>renovar</b>: troca um refresh token válido por um novo par (rotação: o anterior é revogado).
 *       Reapresentar um refresh token já usado indica roubo: todas as sessões do usuário são encerradas;</li>
 *   <li><b>logout</b>: revoga os refresh tokens e incrementa a versão do token, invalidando os access tokens.</li>
 * </ol>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AutenticacaoService {

    private static final SecureRandom GERADOR = new SecureRandom();

    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UsuarioRepository usuarioRepository;
    private final JwtProperties jwtProperties;
    private final Clock clock;

    @Transactional
    public DadosTokenJWT login(DadosLogin dados) {
        var credenciais = new UsernamePasswordAuthenticationToken(dados.email(), dados.senha());
        var usuario = (Usuario) authenticationManager.authenticate(credenciais).getPrincipal();
        log.info("Login realizado: usuário {} ({})", usuario.getId(), usuario.getPerfil());
        return emitirTokens(usuario);
    }

    @Transactional(noRollbackFor = TokenInvalidoException.class)
    public DadosTokenJWT renovar(String refreshToken) {
        var registro = refreshTokenRepository.findByTokenHash(hash(refreshToken))
                .orElseThrow(() -> new TokenInvalidoException(CodigoErro.REFRESH_TOKEN_INVALIDO, "Refresh token inválido."));
        var usuario = registro.getUsuario();

        if (registro.isRevogado()) {
            // Reuso de um refresh token já rotacionado: possível vazamento. Encerra todas as sessões.
            revogarSessoes(usuario);
            log.warn("Reuso de refresh token detectado para o usuário {}. Todas as sessões foram revogadas.", usuario.getId());
            throw new TokenInvalidoException(CodigoErro.REFRESH_TOKEN_REUTILIZADO,
                    "Refresh token já utilizado. Por segurança, todas as sessões deste usuário foram encerradas.");
        }
        if (registro.isExpirado(agora())) {
            throw new TokenInvalidoException(CodigoErro.REFRESH_TOKEN_EXPIRADO, "Refresh token expirado. Faça login novamente.");
        }
        if (!usuario.isEnabled()) {
            throw new TokenInvalidoException(CodigoErro.REFRESH_TOKEN_INVALIDO, "Refresh token inválido.");
        }

        registro.revogar();
        return emitirTokens(usuario);
    }

    @Transactional
    public void logout(Usuario usuarioAutenticado) {
        var usuario = usuarioRepository.findById(usuarioAutenticado.getId()).orElseThrow();
        revogarSessoes(usuario);
        log.info("Logout: tokens do usuário {} revogados", usuario.getId());
    }

    /** Revoga todos os refresh tokens ativos e invalida os access tokens (incrementa a versão). */
    private void revogarSessoes(Usuario usuario) {
        refreshTokenRepository.findAllByUsuario_IdAndRevogadoFalse(usuario.getId()).forEach(RefreshToken::revogar);
        usuario.invalidarTokens();
    }

    private DadosTokenJWT emitirTokens(Usuario usuario) {
        var accessToken = tokenService.gerarAccessToken(usuario);

        String refreshToken = gerarValorAleatorio();
        var criadoEm = agora();
        refreshTokenRepository.save(new RefreshToken(hash(refreshToken), usuario, criadoEm,
                criadoEm.plus(jwtProperties.refreshTokenExpiration())));

        return new DadosTokenJWT(
                accessToken.token(),
                "Bearer",
                jwtProperties.accessTokenExpiration().toSeconds(),
                refreshToken,
                jwtProperties.refreshTokenExpiration().toSeconds(),
                new DadosTokenJWT.UsuarioAutenticado(usuario));
    }

    private LocalDateTime agora() {
        return LocalDateTime.now(clock);
    }

    private static String gerarValorAleatorio() {
        byte[] bytes = new byte[32];
        GERADOR.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    static String hash(String valor) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(valor.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 indisponível", e);
        }
    }
}
