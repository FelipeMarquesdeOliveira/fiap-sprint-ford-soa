package br.com.ford.vinshare.controller;

import br.com.ford.vinshare.support.IntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import static br.com.ford.vinshare.support.DadosDemo.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("Usuários - /usuarios (gestão restrita ao ADMIN)")
class UsuarioControllerTest extends IntegrationTest {

    private static final String NOVO = """
            {"nome": "Gestora Ford Mineira", "email": "concessionaria.mg@vinshare.test", "senha": "Mineira@2026",
             "perfil": "CONCESSIONARIA", "concessionariaId": 3}
            """;

    @Test
    @DisplayName("GET 200: listagem para ADMIN, sem expor senhas")
    void listar() throws Exception {
        mvc.perform(get("/usuarios").header(HttpHeaders.AUTHORIZATION, bearer(tokenAdmin())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page.totalElements").value(4))
                .andExpect(jsonPath("$.content[*].senha").doesNotExist());
    }

    @Test
    @DisplayName("POST 201: cria usuário com senha armazenada em BCrypt e ele consegue fazer login")
    void cadastrarEFazerLogin() throws Exception {
        mvc.perform(post("/usuarios").header(HttpHeaders.AUTHORIZATION, bearer(tokenAdmin()))
                        .contentType(MediaType.APPLICATION_JSON).content(NOVO))
                .andExpect(status().isCreated())
                .andExpect(header().string(HttpHeaders.LOCATION, matchesPattern(".*/usuarios/\\d+$")))
                .andExpect(jsonPath("$.perfil").value("CONCESSIONARIA"))
                .andExpect(jsonPath("$.concessionariaId").value(3))
                .andExpect(jsonPath("$.senha").doesNotExist());

        var senhaSalva = usuarioRepository.findByEmailIgnoreCase("concessionaria.mg@vinshare.test").orElseThrow().getSenha();
        assertThat(senhaSalva).startsWith("$2a$").doesNotContain("Mineira@2026");

        var loginJson = login("concessionaria.mg@vinshare.test", "Mineira@2026");
        assertThat(campo(loginJson, "$.usuario.concessionariaId")).isEqualTo("3");
    }

    @Test
    @DisplayName("POST 400: senha fraca")
    void senhaFraca() throws Exception {
        mvc.perform(post("/usuarios").header(HttpHeaders.AUTHORIZATION, bearer(tokenAdmin()))
                        .contentType(MediaType.APPLICATION_JSON).content(NOVO.replace("Mineira@2026", "12345678")))
                .andExpect(problema(400, "DADOS_INVALIDOS"))
                .andExpect(jsonPath("$.erros[0].campo").value("senha"));
    }

    @Test
    @DisplayName("POST 409: e-mail já cadastrado (sem diferenciar maiúsculas)")
    void emailDuplicado() throws Exception {
        mvc.perform(post("/usuarios").header(HttpHeaders.AUTHORIZATION, bearer(tokenAdmin()))
                        .contentType(MediaType.APPLICATION_JSON).content(NOVO.replace("concessionaria.mg@vinshare.test", "ADMIN@vinshare.test")))
                .andExpect(problema(409, "CONFLITO"));
    }

    @Test
    @DisplayName("POST 422: perfil CONCESSIONARIA exige concessionária existente")
    void concessionariaObrigatoria() throws Exception {
        mvc.perform(post("/usuarios").header(HttpHeaders.AUTHORIZATION, bearer(tokenAdmin()))
                        .contentType(MediaType.APPLICATION_JSON).content(NOVO.replace("\"concessionariaId\": 3", "\"concessionariaId\": 99")))
                .andExpect(problema(422, "REGRA_DE_NEGOCIO"));
    }

    @Test
    @DisplayName("PATCH 200: mudar o perfil de um usuário revoga os tokens que ele já tinha")
    void mudarPerfilRevogaTokens() throws Exception {
        var tokenAntigo = tokenAnalista();

        mvc.perform(patch("/usuarios/{id}", ID_ANALISTA).header(HttpHeaders.AUTHORIZATION, bearer(tokenAdmin()))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"perfil\": \"ADMIN\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.perfil").value("ADMIN"));

        mvc.perform(get("/usuarios/me").header(HttpHeaders.AUTHORIZATION, bearer(tokenAntigo)))
                .andExpect(problema(401, "TOKEN_REVOGADO"));
        mvc.perform(get("/usuarios").header(HttpHeaders.AUTHORIZATION, bearer(tokenAnalista())))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PATCH / DELETE 422: ADMIN não pode rebaixar nem desativar a si mesmo")
    void protecaoDoProprioAdmin() throws Exception {
        mvc.perform(patch("/usuarios/{id}", ID_ADMIN).header(HttpHeaders.AUTHORIZATION, bearer(tokenAdmin()))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"perfil\": \"ANALISTA\"}"))
                .andExpect(problema(422, "REGRA_DE_NEGOCIO"));
        mvc.perform(delete("/usuarios/{id}", ID_ADMIN).header(HttpHeaders.AUTHORIZATION, bearer(tokenAdmin())))
                .andExpect(problema(422, "REGRA_DE_NEGOCIO"));
    }

    @Test
    @DisplayName("DELETE 204: desativa o usuário e bloqueia o token dele; 404 para id inexistente")
    void desativar() throws Exception {
        var tokenDoUsuario = tokenConcessionariaRj();

        mvc.perform(delete("/usuarios/{id}", ID_USUARIO_RJ).header(HttpHeaders.AUTHORIZATION, bearer(tokenAdmin())))
                .andExpect(status().isNoContent());
        mvc.perform(get("/clientes").header(HttpHeaders.AUTHORIZATION, bearer(tokenDoUsuario)))
                .andExpect(problema(401, "TOKEN_REVOGADO"));
        mvc.perform(get("/usuarios/{id}", 999).header(HttpHeaders.AUTHORIZATION, bearer(tokenAdmin())))
                .andExpect(problema(404, "RECURSO_NAO_ENCONTRADO"));
    }
}
