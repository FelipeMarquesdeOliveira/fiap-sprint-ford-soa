package br.com.ford.vinshare.infra.security;

import br.com.ford.vinshare.domain.usuario.Usuario;
import br.com.ford.vinshare.domain.usuario.UsuarioRepository;
import br.com.ford.vinshare.infra.exception.CodigoErro;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Objects;

/**
 * Filtro de autenticação stateless: lê o header "Authorization: Bearer &lt;jwt&gt;",
 * valida o token e registra o usuário no SecurityContext.
 *
 * <p>Além da assinatura e expiração, confere se o usuário (claim "sub") continua ativo
 * e se a versão do token (claim "ver") é a atual, o que permite revogar tokens no logout,
 * na desativação do usuário ou na troca de perfil.</p>
 *
 * <p>O filtro não rejeita a requisição: quando o token é inválido ele apenas registra o
 * motivo em um atributo da requisição. Quem decide se o endpoint exige autenticação é a
 * SecurityFilterChain (endpoints públicos seguem normalmente).</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SecurityFilter extends OncePerRequestFilter {

    public static final String ATRIBUTO_ERRO_TOKEN = SecurityFilter.class.getName() + ".ERRO_TOKEN";
    private static final String PREFIXO_BEARER = "Bearer ";

    private final TokenService tokenService;
    private final UsuarioRepository usuarioRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (header != null && !header.isBlank()) {
            autenticar(header, request);
        }

        filterChain.doFilter(request, response);
    }

    private void autenticar(String header, HttpServletRequest request) {
        if (!header.regionMatches(true, 0, PREFIXO_BEARER, 0, PREFIXO_BEARER.length())) {
            request.setAttribute(ATRIBUTO_ERRO_TOKEN, CodigoErro.TOKEN_INVALIDO);
            return;
        }

        String token = header.substring(PREFIXO_BEARER.length()).trim();
        try {
            DecodedJWT jwt = tokenService.validarAccessToken(token);
            Long usuarioId = Long.valueOf(jwt.getSubject());
            Integer versao = jwt.getClaim(TokenService.CLAIM_VERSAO).asInt();

            usuarioRepository.findById(usuarioId)
                    .filter(Usuario::isEnabled)
                    .filter(usuario -> Objects.equals(usuario.getVersaoToken(), versao))
                    .ifPresentOrElse(
                            usuario -> registrarAutenticacao(usuario, request),
                            () -> request.setAttribute(ATRIBUTO_ERRO_TOKEN, CodigoErro.TOKEN_REVOGADO));
        } catch (TokenExpiredException e) {
            request.setAttribute(ATRIBUTO_ERRO_TOKEN, CodigoErro.TOKEN_EXPIRADO);
        } catch (JWTVerificationException | NumberFormatException e) {
            log.warn("Token JWT rejeitado em {} {}: {}", request.getMethod(), request.getRequestURI(), e.getMessage());
            request.setAttribute(ATRIBUTO_ERRO_TOKEN, CodigoErro.TOKEN_INVALIDO);
        }
    }

    private void registrarAutenticacao(Usuario usuario, HttpServletRequest request) {
        var autenticacao = new UsernamePasswordAuthenticationToken(usuario, null, usuario.getAuthorities());
        autenticacao.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        var contexto = SecurityContextHolder.createEmptyContext();
        contexto.setAuthentication(autenticacao);
        SecurityContextHolder.setContext(contexto);
    }
}
