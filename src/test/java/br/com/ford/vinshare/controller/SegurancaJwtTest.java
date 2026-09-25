package br.com.ford.vinshare.controller;

import br.com.ford.vinshare.support.IntegrationTest;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.time.Instant;

import static br.com.ford.vinshare.support.DadosDemo.*;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("Proteção de recursos com JWT")
class SegurancaJwtTest extends IntegrationTest {

    private static final String SEGREDO_TESTE = "segredo-exclusivo-dos-testes-automatizados-0123456789";

    @ParameterizedTest(name = "200: GET {0} é público")
    @ValueSource(strings = {"/health-check", "/concessionarias", "/concessionarias/1", "/api-docs"})
    @DisplayName("200: endpoints públicos não exigem token")
    void endpointsPublicos(String uri) throws Exception {
        mvc.perform(get(uri)).andExpect(status().isOk());
    }

    @Test
    @DisplayName("200: endpoint público continua acessível mesmo com token inválido")
    void publicoIgnoraTokenInvalido() throws Exception {
        mvc.perform(get("/concessionarias").header(HttpHeaders.AUTHORIZATION, "Bearer token.invalido.qualquer"))
                .andExpect(status().isOk());
    }

    @ParameterizedTest(name = "401: GET {0} sem token")
    @ValueSource(strings = {"/clientes", "/veiculos", "/servicos", "/usuarios/me", "/vinshare/dashboard", "/vinshare/clientes-risco"})
    @DisplayName("401: endpoints protegidos exigem token")
    void endpointsProtegidos(String uri) throws Exception {
        mvc.perform(get(uri))
                .andExpect(problema(401, "NAO_AUTENTICADO"))
                .andExpect(header().string(HttpHeaders.WWW_AUTHENTICATE, "Bearer realm=\"vinshare-api\""));
    }

    @Test
    @DisplayName("401: escrita em recurso de leitura pública também exige token")
    void escritaEmRecursoPublicoExigeToken() throws Exception {
        mvc.perform(post("/concessionarias").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(problema(401, "NAO_AUTENTICADO"));
    }

    @Test
    @DisplayName("200: token válido dá acesso e o usuário é identificado pelo claim sub")
    void tokenValido() throws Exception {
        mvc.perform(get("/usuarios/me").header(HttpHeaders.AUTHORIZATION, bearer(tokenConcessionariaSp())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ID_USUARIO_SP))
                .andExpect(jsonPath("$.email").value(CONCESSIONARIA_SP))
                .andExpect(jsonPath("$.perfil").value("CONCESSIONARIA"))
                .andExpect(jsonPath("$.concessionariaId").value(CONCESSIONARIA_PAULISTA))
                .andExpect(jsonPath("$.senha").doesNotExist());
    }

    @Test
    @DisplayName("401: token expirado")
    void tokenExpirado() throws Exception {
        var expirado = tokenAssinado(Algorithm.HMAC256(SEGREDO_TESTE), "vinshare-api", "vinshare-clients",
                Instant.now().minusSeconds(3600), Instant.now().minusSeconds(60));

        mvc.perform(get("/usuarios/me").header(HttpHeaders.AUTHORIZATION, bearer(expirado)))
                .andExpect(problema(401, "TOKEN_EXPIRADO"))
                .andExpect(header().string(HttpHeaders.WWW_AUTHENTICATE, containsString("error=\"invalid_token\"")));
    }

    @Test
    @DisplayName("401: token assinado com outro segredo")
    void assinaturaInvalida() throws Exception {
        var falsificado = tokenAssinado(Algorithm.HMAC256("segredo-de-um-atacante-com-mais-de-32-bytes"),
                "vinshare-api", "vinshare-clients", Instant.now(), Instant.now().plusSeconds(600));

        mvc.perform(get("/usuarios/me").header(HttpHeaders.AUTHORIZATION, bearer(falsificado)))
                .andExpect(problema(401, "TOKEN_INVALIDO"));
    }

    @Test
    @DisplayName("401: token sem assinatura (alg none)")
    void algoritmoNone() throws Exception {
        var semAssinatura = tokenAssinado(Algorithm.none(), "vinshare-api", "vinshare-clients",
                Instant.now(), Instant.now().plusSeconds(600));

        mvc.perform(get("/usuarios/me").header(HttpHeaders.AUTHORIZATION, bearer(semAssinatura)))
                .andExpect(problema(401, "TOKEN_INVALIDO"));
    }

    @Test
    @DisplayName("401: token emitido para outra audiência")
    void audienciaIncorreta() throws Exception {
        var outraAudiencia = tokenAssinado(Algorithm.HMAC256(SEGREDO_TESTE), "vinshare-api", "outro-sistema",
                Instant.now(), Instant.now().plusSeconds(600));

        mvc.perform(get("/usuarios/me").header(HttpHeaders.AUTHORIZATION, bearer(outraAudiencia)))
                .andExpect(problema(401, "TOKEN_INVALIDO"));
    }

    @ParameterizedTest(name = "401: header Authorization \"{0}\"")
    @ValueSource(strings = {"Bearer isto-nao-e-jwt", "Basic YWRtaW46YWRtaW4=", "Token abc"})
    @DisplayName("401: header Authorization malformado")
    void headerMalformado(String header) throws Exception {
        mvc.perform(get("/clientes").header(HttpHeaders.AUTHORIZATION, header))
                .andExpect(problema(401, "TOKEN_INVALIDO"));
    }

    @Test
    @DisplayName("401: token de usuário desativado deixa de valer imediatamente")
    void usuarioDesativado() throws Exception {
        var token = tokenAnalista();
        usuarioRepository.findById(ID_ANALISTA).orElseThrow().desativar();

        mvc.perform(get("/clientes").header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(problema(401, "TOKEN_REVOGADO"));
    }

    @Test
    @DisplayName("403: claim 'perfil' forjado não concede privilégios (permissões vêm do cadastro do usuário)")
    void perfilNaoPodeSerForjado() throws Exception {
        // Mesmo com a assinatura correta, um token do usuário CONCESSIONARIA (sub=3) com perfil=ADMIN não vira ADMIN
        var forjado = JWT.create()
                .withIssuer("vinshare-api").withAudience("vinshare-clients")
                .withSubject(String.valueOf(ID_USUARIO_SP))
                .withExpiresAt(Instant.now().plusSeconds(600))
                .withClaim("tipo", "access").withClaim("perfil", "ADMIN").withClaim("ver", 0)
                .sign(Algorithm.HMAC256(SEGREDO_TESTE));

        mvc.perform(get("/usuarios").header(HttpHeaders.AUTHORIZATION, bearer(forjado)))
                .andExpect(problema(403, "ACESSO_NEGADO"));
    }

    /** Token do admin (sub=1, ver=0) com parâmetros controlados pelo teste. */
    private static String tokenAssinado(Algorithm algoritmo, String emissor, String audiencia, Instant emitidoEm, Instant expiraEm) {
        return JWT.create()
                .withIssuer(emissor)
                .withAudience(audiencia)
                .withSubject(String.valueOf(ID_ADMIN))
                .withIssuedAt(emitidoEm)
                .withExpiresAt(expiraEm)
                .withClaim("tipo", "access")
                .withClaim("perfil", "ADMIN")
                .withClaim("ver", 0)
                .sign(algoritmo);
    }
}
