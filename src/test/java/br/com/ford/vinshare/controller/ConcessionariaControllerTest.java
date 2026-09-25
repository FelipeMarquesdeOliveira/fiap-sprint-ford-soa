package br.com.ford.vinshare.controller;

import br.com.ford.vinshare.support.IntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import static br.com.ford.vinshare.support.DadosDemo.*;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("Concessionárias - /concessionarias")
class ConcessionariaControllerTest extends IntegrationTest {

    private static final String NOVA = """
            {"cnpj": "55.666.777/0001-81", "nome": "Ford Capixaba Vitória", "regiao": "Sudeste", "cidade": "Vitória", "estado": "ES"}
            """;

    @Test
    @DisplayName("GET 200: lista paginada e ordenada por nome, sem autenticação")
    void listarPaginado() throws Exception {
        mvc.perform(get("/concessionarias").param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].nome").value("Ford Carioca Barra"))
                .andExpect(jsonPath("$.page.size").value(2))
                .andExpect(jsonPath("$.page.totalElements").value(4))
                .andExpect(jsonPath("$.page.totalPages").value(2));
    }

    @Test
    @DisplayName("GET 200: filtro por UF")
    void filtrarPorEstado() throws Exception {
        mvc.perform(get("/concessionarias").param("estado", "RS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].cidade").value("Porto Alegre"));
    }

    @Test
    @DisplayName("GET 200 / 404: detalhe por id")
    void detalhar() throws Exception {
        mvc.perform(get("/concessionarias/{id}", CONCESSIONARIA_PAULISTA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cnpj").value(CNPJ_PAULISTA))
                .andExpect(jsonPath("$.ativa").value(true));

        mvc.perform(get("/concessionarias/{id}", 999)).andExpect(problema(404, "RECURSO_NAO_ENCONTRADO"));
    }

    @Test
    @DisplayName("POST 201: cria e devolve Location apontando para o novo recurso")
    void cadastrar() throws Exception {
        var location = mvc.perform(post("/concessionarias").header(HttpHeaders.AUTHORIZATION, bearer(tokenAdmin()))
                        .contentType(MediaType.APPLICATION_JSON).content(NOVA))
                .andExpect(status().isCreated())
                .andExpect(header().string(HttpHeaders.LOCATION, matchesPattern(".*/concessionarias/\\d+$")))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.nome").value("Ford Capixaba Vitória"))
                .andExpect(jsonPath("$.ativa").value(true))
                .andReturn().getResponse().getHeader(HttpHeaders.LOCATION);

        mvc.perform(get(location)).andExpect(status().isOk()).andExpect(jsonPath("$.estado").value("ES"));
    }

    @Test
    @DisplayName("POST 409: CNPJ já cadastrado")
    void cnpjDuplicado() throws Exception {
        mvc.perform(post("/concessionarias").header(HttpHeaders.AUTHORIZATION, bearer(tokenAdmin()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(NOVA.replace(CNPJ_NOVO, CNPJ_PAULISTA)))
                .andExpect(problema(409, "CONFLITO"));
    }

    @Test
    @DisplayName("POST 400: CNPJ com dígito verificador inválido, nome vazio e UF inválida")
    void dadosInvalidos() throws Exception {
        mvc.perform(post("/concessionarias").header(HttpHeaders.AUTHORIZATION, bearer(tokenAdmin()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"cnpj": "11.222.333/0001-00", "nome": "", "regiao": "Sul", "estado": "rs"}
                                """))
                .andExpect(problema(400, "DADOS_INVALIDOS"))
                .andExpect(jsonPath("$.erros[*].campo", containsInAnyOrder("cnpj", "nome", "estado")));
    }

    @Test
    @DisplayName("PUT 200: substitui a representação completa")
    void substituir() throws Exception {
        mvc.perform(put("/concessionarias/{id}", CONCESSIONARIA_GAUCHA).header(HttpHeaders.AUTHORIZATION, bearer(tokenAdmin()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"cnpj": "22.111.444/0001-37", "nome": "Ford Gaúcha Centro", "regiao": "Sul", "cidade": "Canoas", "estado": "RS"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Ford Gaúcha Centro"))
                .andExpect(jsonPath("$.cidade").value("Canoas"));
    }

    @Test
    @DisplayName("PUT 400: representação incompleta é rejeitada (PUT não é atualização parcial)")
    void substituirIncompleto() throws Exception {
        mvc.perform(put("/concessionarias/{id}", CONCESSIONARIA_GAUCHA).header(HttpHeaders.AUTHORIZATION, bearer(tokenAdmin()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\": \"Somente o nome\"}"))
                .andExpect(problema(400, "DADOS_INVALIDOS"))
                .andExpect(jsonPath("$.erros[*].campo", hasItems("cnpj", "regiao")));
    }

    @Test
    @DisplayName("PUT 409: CNPJ pertencente a outra concessionária")
    void substituirComCnpjDeOutra() throws Exception {
        mvc.perform(put("/concessionarias/{id}", CONCESSIONARIA_GAUCHA).header(HttpHeaders.AUTHORIZATION, bearer(tokenAdmin()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(NOVA.replace(CNPJ_NOVO, CNPJ_PAULISTA)))
                .andExpect(problema(409, "CONFLITO"));
    }

    @Test
    @DisplayName("PATCH 200: altera apenas os campos enviados")
    void atualizarParcialmente() throws Exception {
        mvc.perform(patch("/concessionarias/{id}", CONCESSIONARIA_MINEIRA).header(HttpHeaders.AUTHORIZATION, bearer(tokenAdmin()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\": \"Ford Mineira Pampulha\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Ford Mineira Pampulha"))
                .andExpect(jsonPath("$.cidade").value("Belo Horizonte"))
                .andExpect(jsonPath("$.cnpj").value("77.888.999/0001-81"));
    }

    @Test
    @DisplayName("DELETE 204: exclusão lógica; depois o recurso responde 404")
    void excluir() throws Exception {
        mvc.perform(delete("/concessionarias/{id}", CONCESSIONARIA_MINEIRA).header(HttpHeaders.AUTHORIZATION, bearer(tokenAdmin())))
                .andExpect(status().isNoContent());

        mvc.perform(get("/concessionarias/{id}", CONCESSIONARIA_MINEIRA)).andExpect(problema(404, "RECURSO_NAO_ENCONTRADO"));
        mvc.perform(delete("/concessionarias/{id}", CONCESSIONARIA_MINEIRA).header(HttpHeaders.AUTHORIZATION, bearer(tokenAdmin())))
                .andExpect(problema(404, "RECURSO_NAO_ENCONTRADO"));
    }

    @Test
    @DisplayName("403: perfil sem permissão de escrita")
    void escritaSemPermissao() throws Exception {
        mvc.perform(post("/concessionarias").header(HttpHeaders.AUTHORIZATION, bearer(tokenAnalista()))
                        .contentType(MediaType.APPLICATION_JSON).content(NOVA))
                .andExpect(problema(403, "ACESSO_NEGADO"));
    }
}
