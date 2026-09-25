package br.com.ford.vinshare.infra.security;

import br.com.ford.vinshare.domain.usuario.DadosCadastroUsuario;
import br.com.ford.vinshare.domain.usuario.Perfil;
import br.com.ford.vinshare.domain.usuario.Usuario;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("TokenService - geração e validação de JWT")
class TokenServiceTest {

    private static final String SEGREDO = "segredo-de-teste-unitario-com-mais-de-32-bytes";
    private static final JwtProperties PROPRIEDADES = new JwtProperties(
            SEGREDO, "vinshare-api", "vinshare-clients", Duration.ofMinutes(15), Duration.ofDays(7));

    private final TokenService tokenService = new TokenService(PROPRIEDADES, Clock.systemUTC());

    @Test
    @DisplayName("gera token com emissor, audiência, subject, perfil, versão, jti e expiração de 15 minutos")
    void geraTokenComClaimsEsperados() {
        var gerado = tokenService.gerarAccessToken(usuario(7L, Perfil.ANALISTA, null));

        var jwt = JWT.decode(gerado.token());
        assertThat(jwt.getAlgorithm()).isEqualTo("HS256");
        assertThat(jwt.getIssuer()).isEqualTo("vinshare-api");
        assertThat(jwt.getAudience()).containsExactly("vinshare-clients");
        assertThat(jwt.getSubject()).isEqualTo("7");
        assertThat(jwt.getId()).isNotBlank();
        assertThat(jwt.getClaim(TokenService.CLAIM_PERFIL).asString()).isEqualTo("ANALISTA");
        assertThat(jwt.getClaim(TokenService.CLAIM_VERSAO).asInt()).isZero();
        assertThat(jwt.getClaim(TokenService.CLAIM_TIPO).asString()).isEqualTo("access");
        assertThat(jwt.getClaim(TokenService.CLAIM_CONCESSIONARIA).isMissing()).isTrue();
        assertThat(Duration.between(jwt.getIssuedAtAsInstant(), jwt.getExpiresAtAsInstant())).isEqualTo(Duration.ofMinutes(15));
        assertThat(gerado.expiraEm()).isEqualTo(jwt.getExpiresAtAsInstant());
    }

    @Test
    @DisplayName("inclui o claim concessionariaId apenas para o perfil CONCESSIONARIA")
    void incluiConcessionariaParaPerfilConcessionaria() {
        var token = tokenService.gerarAccessToken(usuario(3L, Perfil.CONCESSIONARIA, 1L)).token();

        assertThat(JWT.decode(token).getClaim(TokenService.CLAIM_CONCESSIONARIA).asLong()).isEqualTo(1L);
    }

    @Test
    @DisplayName("não coloca a senha nem o hash da senha no token")
    void naoExpoeSenhaNoToken() {
        var token = tokenService.gerarAccessToken(usuario(1L, Perfil.ADMIN, null)).token();

        var claims = JWT.decode(token).getClaims();
        assertThat(claims).doesNotContainKeys("senha", "password");
        assertThat(claims.values().toString()).doesNotContain("$2a$");
    }

    @Test
    @DisplayName("valida um token íntegro e devolve seus claims")
    void validaTokenIntegro() {
        var token = tokenService.gerarAccessToken(usuario(1L, Perfil.ADMIN, null)).token();

        var validado = tokenService.validarAccessToken(token);

        assertThat(validado.getSubject()).isEqualTo("1");
        assertThat(validado.getClaim(TokenService.CLAIM_PERFIL).asString()).isEqualTo("ADMIN");
    }

    @Test
    @DisplayName("rejeita token expirado")
    void rejeitaTokenExpirado() {
        var relogioNoPassado = Clock.fixed(Instant.now().minus(Duration.ofHours(1)), ZoneOffset.UTC);
        var token = new TokenService(PROPRIEDADES, relogioNoPassado).gerarAccessToken(usuario(1L, Perfil.ADMIN, null)).token();

        assertThatThrownBy(() -> tokenService.validarAccessToken(token)).isInstanceOf(TokenExpiredException.class);
    }

    @Test
    @DisplayName("rejeita token assinado com outro segredo")
    void rejeitaAssinaturaInvalida() {
        var outroServico = new TokenService(new JwtProperties("outro-segredo-qualquer-com-mais-de-32-bytes",
                "vinshare-api", "vinshare-clients", Duration.ofMinutes(15), Duration.ofDays(7)), Clock.systemUTC());
        var token = outroServico.gerarAccessToken(usuario(1L, Perfil.ADMIN, null)).token();

        assertThatThrownBy(() -> tokenService.validarAccessToken(token)).isInstanceOf(SignatureVerificationException.class);
    }

    @Test
    @DisplayName("rejeita token com payload adulterado (ex.: troca de perfil)")
    void rejeitaPayloadAdulterado() {
        var token = tokenService.gerarAccessToken(usuario(3L, Perfil.CONCESSIONARIA, 1L)).token();
        var partes = token.split("\\.");
        var payloadAdulterado = java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(
                new String(java.util.Base64.getUrlDecoder().decode(partes[1])).replace("CONCESSIONARIA", "ADMIN").getBytes());

        assertThatThrownBy(() -> tokenService.validarAccessToken(partes[0] + "." + payloadAdulterado + "." + partes[2]))
                .isInstanceOf(SignatureVerificationException.class);
    }

    @Test
    @DisplayName("rejeita token sem assinatura (alg: none)")
    void rejeitaAlgoritmoNone() {
        var token = JWT.create()
                .withIssuer("vinshare-api").withAudience("vinshare-clients").withSubject("1")
                .withClaim("tipo", "access").withClaim("perfil", "ADMIN").withClaim("ver", 0)
                .withExpiresAt(Date.from(Instant.now().plusSeconds(600)))
                .sign(Algorithm.none());

        assertThatThrownBy(() -> tokenService.validarAccessToken(token)).isInstanceOf(AlgorithmMismatchException.class);
    }

    @Test
    @DisplayName("rejeita token de outro emissor ou para outra audiência")
    void rejeitaEmissorOuAudienciaIncorretos() {
        var algoritmo = Algorithm.HMAC256(SEGREDO);
        var outroEmissor = JWT.create().withIssuer("outra-api").withAudience("vinshare-clients").withSubject("1")
                .withClaim("tipo", "access").withClaim("perfil", "ADMIN").withClaim("ver", 0).sign(algoritmo);
        var outraAudiencia = JWT.create().withIssuer("vinshare-api").withAudience("outro-cliente").withSubject("1")
                .withClaim("tipo", "access").withClaim("perfil", "ADMIN").withClaim("ver", 0).sign(algoritmo);

        assertThatThrownBy(() -> tokenService.validarAccessToken(outroEmissor)).isInstanceOf(IncorrectClaimException.class);
        assertThatThrownBy(() -> tokenService.validarAccessToken(outraAudiencia)).isInstanceOf(IncorrectClaimException.class);
    }

    @Test
    @DisplayName("rejeita token que não é do tipo access")
    void rejeitaTipoDiferenteDeAccess() {
        var token = JWT.create().withIssuer("vinshare-api").withAudience("vinshare-clients").withSubject("1")
                .withClaim("tipo", "refresh").withClaim("perfil", "ADMIN").withClaim("ver", 0)
                .sign(Algorithm.HMAC256(SEGREDO));

        assertThatThrownBy(() -> tokenService.validarAccessToken(token)).isInstanceOf(IncorrectClaimException.class);
    }

    @Test
    @DisplayName("rejeita texto que não é um JWT")
    void rejeitaTokenMalformado() {
        assertThatThrownBy(() -> tokenService.validarAccessToken("isto-nao-e-um-jwt")).isInstanceOf(JWTDecodeException.class);
    }

    @Test
    @DisplayName("não inicializa com segredo menor que 32 bytes")
    void exigeSegredoForte() {
        var fraco = new JwtProperties("curto", "vinshare-api", "vinshare-clients", Duration.ofMinutes(15), Duration.ofDays(7));

        assertThatThrownBy(() -> new TokenService(fraco, Clock.systemUTC()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("32 bytes");
    }

    private static Usuario usuario(Long id, Perfil perfil, Long concessionariaId) {
        var usuario = new Usuario(new DadosCadastroUsuario("Teste", "teste@vinshare.test", "Senha@123", perfil, concessionariaId),
                "$2a$10$hashqualquerparaoteste");
        ReflectionTestUtils.setField(usuario, "id", id);
        return usuario;
    }
}
