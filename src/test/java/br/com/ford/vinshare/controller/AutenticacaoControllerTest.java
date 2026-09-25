package br.com.ford.vinshare.controller;

import br.com.ford.vinshare.domain.autenticacao.RefreshToken;
import br.com.ford.vinshare.domain.autenticacao.RefreshTokenRepository;
import br.com.ford.vinshare.support.IntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.HexFormat;

import static br.com.ford.vinshare.support.DadosDemo.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("Autenticação - /auth")
class AutenticacaoControllerTest extends IntegrationTest {

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Nested
    @DisplayName("POST /auth/login")
    class Login {

        @Test
        @DisplayName("200: credenciais válidas retornam access token, refresh token e dados do usuário")
        void loginComSucesso() throws Exception {
            mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"email": "admin@vinshare.test", "senha": "Admin@123"}
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.accessToken", matchesPattern("^[\\w-]+\\.[\\w-]+\\.[\\w-]+$")))
                    .andExpect(jsonPath("$.tokenType").value("Bearer"))
                    .andExpect(jsonPath("$.expiresIn").value(900))
                    .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                    .andExpect(jsonPath("$.refreshExpiresIn").value(604800))
                    .andExpect(jsonPath("$.usuario.email").value(ADMIN))
                    .andExpect(jsonPath("$.usuario.perfil").value("ADMIN"))
                    .andExpect(jsonPath("$.usuario.senha").doesNotExist());
        }

        @Test
        @DisplayName("200: e-mail não diferencia maiúsculas/minúsculas e o token do perfil CONCESSIONARIA traz a concessionária")
        void loginConcessionaria() throws Exception {
            var resposta = login("Concessionaria.SP@vinshare.test", SENHA_CONCESSIONARIA);

            assertThat(campo(resposta, "$.usuario.perfil")).isEqualTo("CONCESSIONARIA");
            assertThat(campo(resposta, "$.usuario.concessionariaId")).isEqualTo("1");
        }

        @Test
        @DisplayName("401: senha incorreta")
        void senhaIncorreta() throws Exception {
            mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"email": "admin@vinshare.test", "senha": "SenhaErrada@1"}
                                    """))
                    .andExpect(problema(401, "CREDENCIAIS_INVALIDAS"))
                    .andExpect(header().string(HttpHeaders.WWW_AUTHENTICATE, startsWith("Bearer")));
        }

        @Test
        @DisplayName("401: usuário inexistente recebe a mesma resposta (sem enumeração de usuários)")
        void usuarioInexistente() throws Exception {
            mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"email": "ninguem@vinshare.test", "senha": "Admin@123"}
                                    """))
                    .andExpect(problema(401, "CREDENCIAIS_INVALIDAS"))
                    .andExpect(jsonPath("$.detail").value("E-mail ou senha inválidos."));
        }

        @Test
        @DisplayName("401: usuário desativado não consegue autenticar")
        void usuarioDesativado() throws Exception {
            usuarioRepository.findById(ID_ANALISTA).orElseThrow().desativar();

            mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"email": "analista@vinshare.test", "senha": "Analista@123"}
                                    """))
                    .andExpect(problema(401, "CREDENCIAIS_INVALIDAS"));
        }

        @Test
        @DisplayName("400: corpo com e-mail inválido e senha vazia lista os campos com erro")
        void dadosInvalidos() throws Exception {
            mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"email": "nao-e-email", "senha": ""}
                                    """))
                    .andExpect(problema(400, "DADOS_INVALIDOS"))
                    .andExpect(jsonPath("$.erros[*].campo", containsInAnyOrder("email", "senha")));
        }

        @Test
        @DisplayName("400: JSON malformado")
        void jsonMalformado() throws Exception {
            mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON).content("{\"email\": "))
                    .andExpect(problema(400, "REQUISICAO_INVALIDA"));
        }

        @Test
        @DisplayName("415: Content-Type diferente de JSON")
        void tipoDeMidiaNaoSuportado() throws Exception {
            mvc.perform(post("/auth/login").contentType(MediaType.TEXT_PLAIN).content("admin"))
                    .andExpect(problema(415, "TIPO_DE_MIDIA_NAO_SUPORTADO"));
        }
    }

    @Nested
    @DisplayName("POST /auth/refresh")
    class Refresh {

        @Test
        @DisplayName("200: refresh token válido gera um novo par e o anterior é revogado (rotação)")
        void renovaTokens() throws Exception {
            var loginJson = login(ADMIN, SENHA_ADMIN);
            var refreshOriginal = campo(loginJson, "$.refreshToken");

            var renovado = mvc.perform(post("/auth/refresh").contentType(MediaType.APPLICATION_JSON)
                            .content("{\"refreshToken\": \"%s\"}".formatted(refreshOriginal)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accessToken").isNotEmpty())
                    .andExpect(jsonPath("$.refreshToken", not(refreshOriginal)))
                    .andReturn().getResponse().getContentAsString();

            assertThat(refreshTokenRepository.findByTokenHash(sha256(refreshOriginal)).orElseThrow().isRevogado()).isTrue();
            mvc.perform(get("/usuarios/me").header(HttpHeaders.AUTHORIZATION, bearer(campo(renovado, "$.accessToken"))))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("401: reutilizar um refresh token já usado encerra todas as sessões do usuário")
        void deteccaoDeReuso() throws Exception {
            var refreshOriginal = campo(login(ADMIN, SENHA_ADMIN), "$.refreshToken");
            var renovado = mvc.perform(post("/auth/refresh").contentType(MediaType.APPLICATION_JSON)
                            .content("{\"refreshToken\": \"%s\"}".formatted(refreshOriginal)))
                    .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

            mvc.perform(post("/auth/refresh").contentType(MediaType.APPLICATION_JSON)
                            .content("{\"refreshToken\": \"%s\"}".formatted(refreshOriginal)))
                    .andExpect(problema(401, "REFRESH_TOKEN_REUTILIZADO"));

            // o par emitido na rotação também deixa de valer
            mvc.perform(get("/usuarios/me").header(HttpHeaders.AUTHORIZATION, bearer(campo(renovado, "$.accessToken"))))
                    .andExpect(problema(401, "TOKEN_REVOGADO"));
            mvc.perform(post("/auth/refresh").contentType(MediaType.APPLICATION_JSON)
                            .content("{\"refreshToken\": \"%s\"}".formatted(campo(renovado, "$.refreshToken"))))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("401: refresh token desconhecido")
        void refreshInexistente() throws Exception {
            mvc.perform(post("/auth/refresh").contentType(MediaType.APPLICATION_JSON)
                            .content("{\"refreshToken\": \"token-que-nunca-existiu\"}"))
                    .andExpect(problema(401, "REFRESH_TOKEN_INVALIDO"));
        }

        @Test
        @DisplayName("401: refresh token expirado")
        void refreshExpirado() throws Exception {
            var usuario = usuarioRepository.findById(ID_ADMIN).orElseThrow();
            var ontem = LocalDateTime.now().minusDays(8);
            refreshTokenRepository.save(new RefreshToken(sha256("refresh-expirado"), usuario, ontem, ontem.plusDays(7)));

            mvc.perform(post("/auth/refresh").contentType(MediaType.APPLICATION_JSON)
                            .content("{\"refreshToken\": \"refresh-expirado\"}"))
                    .andExpect(problema(401, "REFRESH_TOKEN_EXPIRADO"));
        }

        @Test
        @DisplayName("400: refresh token ausente")
        void refreshAusente() throws Exception {
            mvc.perform(post("/auth/refresh").contentType(MediaType.APPLICATION_JSON).content("{}"))
                    .andExpect(problema(400, "DADOS_INVALIDOS"))
                    .andExpect(jsonPath("$.erros[0].campo").value("refreshToken"));
        }
    }

    @Nested
    @DisplayName("POST /auth/logout")
    class Logout {

        @Test
        @DisplayName("204: logout revoga o access token e o refresh token imediatamente")
        void logoutRevogaTokens() throws Exception {
            var loginJson = login(ANALISTA, SENHA_ANALISTA);
            var accessToken = campo(loginJson, "$.accessToken");

            mvc.perform(post("/auth/logout").header(HttpHeaders.AUTHORIZATION, bearer(accessToken)))
                    .andExpect(status().isNoContent())
                    .andExpect(content().string(""));

            mvc.perform(get("/usuarios/me").header(HttpHeaders.AUTHORIZATION, bearer(accessToken)))
                    .andExpect(problema(401, "TOKEN_REVOGADO"));
            mvc.perform(post("/auth/refresh").contentType(MediaType.APPLICATION_JSON)
                            .content("{\"refreshToken\": \"%s\"}".formatted(campo(loginJson, "$.refreshToken"))))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("401: logout exige autenticação")
        void logoutSemToken() throws Exception {
            mvc.perform(post("/auth/logout")).andExpect(problema(401, "NAO_AUTENTICADO"));
        }
    }

    private static String sha256(String valor) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(valor.getBytes(StandardCharsets.UTF_8)));
    }
}
