package br.com.ford.vinshare.infra.security;

import br.com.ford.vinshare.domain.usuario.Usuario;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

/**
 * Geração e validação dos access tokens (JWT assinado com HMAC-SHA256).
 *
 * <p>Claims emitidos: iss, aud, sub (id do usuário), jti, iat, nbf, exp e os claims
 * privados tipo, email, nome, perfil, concessionariaId (somente perfil CONCESSIONARIA)
 * e ver (versão do token, usada para revogação).</p>
 */
@Service
public class TokenService {

    public static final String CLAIM_TIPO = "tipo";
    public static final String CLAIM_EMAIL = "email";
    public static final String CLAIM_NOME = "nome";
    public static final String CLAIM_PERFIL = "perfil";
    public static final String CLAIM_CONCESSIONARIA = "concessionariaId";
    public static final String CLAIM_VERSAO = "ver";
    public static final String TIPO_ACCESS = "access";

    private static final int TAMANHO_MINIMO_SEGREDO = 32;

    private final JwtProperties propriedades;
    private final Clock clock;
    private final Algorithm algoritmo;
    private final JWTVerifier verificador;

    public TokenService(JwtProperties propriedades, Clock clock) {
        if (propriedades.secret() == null
                || propriedades.secret().getBytes(StandardCharsets.UTF_8).length < TAMANHO_MINIMO_SEGREDO) {
            throw new IllegalStateException(
                    "api.security.jwt.secret deve ter no mínimo " + TAMANHO_MINIMO_SEGREDO + " bytes para HS256");
        }
        this.propriedades = propriedades;
        this.clock = clock;
        this.algoritmo = Algorithm.HMAC256(propriedades.secret());
        // O verificador fixa o algoritmo esperado: tokens com "alg": "none" ou outro algoritmo são rejeitados.
        this.verificador = JWT.require(algoritmo)
                .withIssuer(propriedades.issuer())
                .withAudience(propriedades.audience())
                .withClaim(CLAIM_TIPO, TIPO_ACCESS)
                .withClaimPresence(CLAIM_PERFIL)
                .withClaimPresence(CLAIM_VERSAO)
                .acceptLeeway(1)
                .build();
    }

    public TokenGerado gerarAccessToken(Usuario usuario) {
        // JWT trabalha com precisão de segundos (NumericDate)
        Instant agora = clock.instant().truncatedTo(ChronoUnit.SECONDS);
        Instant expiracao = agora.plus(propriedades.accessTokenExpiration());

        var builder = JWT.create()
                .withIssuer(propriedades.issuer())
                .withAudience(propriedades.audience())
                .withSubject(String.valueOf(usuario.getId()))
                .withJWTId(UUID.randomUUID().toString())
                .withIssuedAt(agora)
                .withNotBefore(agora)
                .withExpiresAt(expiracao)
                .withClaim(CLAIM_TIPO, TIPO_ACCESS)
                .withClaim(CLAIM_EMAIL, usuario.getEmail())
                .withClaim(CLAIM_NOME, usuario.getNome())
                .withClaim(CLAIM_PERFIL, usuario.getPerfil().name())
                .withClaim(CLAIM_VERSAO, usuario.getVersaoToken());
        if (usuario.getConcessionariaId() != null) {
            builder.withClaim(CLAIM_CONCESSIONARIA, usuario.getConcessionariaId());
        }
        return new TokenGerado(builder.sign(algoritmo), expiracao);
    }

    /**
     * Valida assinatura, algoritmo, emissor, audiência, tipo e janela de validade (nbf/exp).
     *
     * @throws com.auth0.jwt.exceptions.TokenExpiredException se o token estiver expirado
     * @throws JWTVerificationException para qualquer outra falha de validação
     */
    public DecodedJWT validarAccessToken(String token) {
        return verificador.verify(token);
    }

    public record TokenGerado(String token, Instant expiraEm) {}
}
