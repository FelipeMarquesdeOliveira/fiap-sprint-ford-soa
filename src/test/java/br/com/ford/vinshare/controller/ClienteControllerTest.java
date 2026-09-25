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

@DisplayName("Clientes - /clientes (escopo por concessionária)")
class ClienteControllerTest extends IntegrationTest {

    private static final String NOVO_SEM_CONCESSIONARIA = """
            {"cpf": "173.205.080-52", "nome": "Larissa Teixeira", "email": "larissa@email.test", "telefone": "(11) 97777-1234",
             "idade": 34, "sexo": "F", "regiao": "Sudeste", "dataCompra": "2025-09-15", "perfilCliente": "FIEL"}
            """;

    @Test
    @DisplayName("GET 200: ADMIN e ANALISTA veem todos os clientes")
    void listarTodos() throws Exception {
        mvc.perform(get("/clientes").header(HttpHeaders.AUTHORIZATION, bearer(tokenAnalista())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page.totalElements").value(10));
    }

    @Test
    @DisplayName("GET 200: CONCESSIONARIA vê apenas os clientes da própria concessionária")
    void listarSomenteDaConcessionaria() throws Exception {
        mvc.perform(get("/clientes").header(HttpHeaders.AUTHORIZATION, bearer(tokenConcessionariaSp())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page.totalElements").value(3))
                .andExpect(jsonPath("$.content[*].concessionariaId", everyItem(is((int) CONCESSIONARIA_PAULISTA))));
    }

    @Test
    @DisplayName("GET 200 / 403 / 404: detalhe respeita o escopo da concessionária")
    void detalharComEscopo() throws Exception {
        mvc.perform(get("/clientes/{id}", CLIENTE_ANA_SP).header(HttpHeaders.AUTHORIZATION, bearer(tokenConcessionariaSp())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cpf").value(CPF_ANA))
                .andExpect(jsonPath("$.veiculo.vin").value(VIN_ANA))
                .andExpect(jsonPath("$.concessionaria.id").value(CONCESSIONARIA_PAULISTA));

        mvc.perform(get("/clientes/{id}", CLIENTE_DIEGO_RJ).header(HttpHeaders.AUTHORIZATION, bearer(tokenConcessionariaSp())))
                .andExpect(problema(403, "ACESSO_NEGADO"));

        mvc.perform(get("/clientes/{id}", 999).header(HttpHeaders.AUTHORIZATION, bearer(tokenAdmin())))
                .andExpect(problema(404, "RECURSO_NAO_ENCONTRADO"));
    }

    @Test
    @DisplayName("POST 201: para CONCESSIONARIA a concessionária vem do token, não do corpo")
    void cadastrarPelaConcessionaria() throws Exception {
        mvc.perform(post("/clientes").header(HttpHeaders.AUTHORIZATION, bearer(tokenConcessionariaRj()))
                        .contentType(MediaType.APPLICATION_JSON).content(NOVO_SEM_CONCESSIONARIA))
                .andExpect(status().isCreated())
                .andExpect(header().string(HttpHeaders.LOCATION, matchesPattern(".*/clientes/\\d+$")))
                .andExpect(jsonPath("$.concessionaria.id").value(CONCESSIONARIA_CARIOCA))
                .andExpect(jsonPath("$.ativo").value(true));
    }

    @Test
    @DisplayName("POST 403: CONCESSIONARIA não cadastra cliente para outra concessionária")
    void cadastrarParaOutraConcessionaria() throws Exception {
        mvc.perform(post("/clientes").header(HttpHeaders.AUTHORIZATION, bearer(tokenConcessionariaRj()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(NOVO_SEM_CONCESSIONARIA.replace("\"perfilCliente\"", "\"concessionariaId\": 1, \"perfilCliente\"")))
                .andExpect(problema(403, "ACESSO_NEGADO"));
    }

    @Test
    @DisplayName("POST 422: ADMIN precisa informar a concessionária")
    void adminSemConcessionaria() throws Exception {
        mvc.perform(post("/clientes").header(HttpHeaders.AUTHORIZATION, bearer(tokenAdmin()))
                        .contentType(MediaType.APPLICATION_JSON).content(NOVO_SEM_CONCESSIONARIA))
                .andExpect(problema(422, "REGRA_DE_NEGOCIO"));
    }

    @Test
    @DisplayName("POST 422: veículo inexistente")
    void veiculoInexistente() throws Exception {
        mvc.perform(post("/clientes").header(HttpHeaders.AUTHORIZATION, bearer(tokenConcessionariaSp()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(NOVO_SEM_CONCESSIONARIA.replace("\"perfilCliente\"", "\"veiculoId\": 999, \"perfilCliente\"")))
                .andExpect(problema(422, "REGRA_DE_NEGOCIO"));
    }

    @Test
    @DisplayName("POST 409: CPF duplicado ou veículo já vinculado a outro cliente")
    void conflitos() throws Exception {
        mvc.perform(post("/clientes").header(HttpHeaders.AUTHORIZATION, bearer(tokenConcessionariaSp()))
                        .contentType(MediaType.APPLICATION_JSON).content(NOVO_SEM_CONCESSIONARIA.replace(CPF_NOVO, CPF_ANA)))
                .andExpect(problema(409, "CONFLITO"));

        mvc.perform(post("/clientes").header(HttpHeaders.AUTHORIZATION, bearer(tokenConcessionariaSp()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(NOVO_SEM_CONCESSIONARIA.replace("\"perfilCliente\"", "\"veiculoId\": 1, \"perfilCliente\"")))
                .andExpect(problema(409, "CONFLITO"));
    }

    @Test
    @DisplayName("POST 400: CPF com dígito verificador inválido, e-mail e sexo inválidos")
    void dadosInvalidos() throws Exception {
        mvc.perform(post("/clientes").header(HttpHeaders.AUTHORIZATION, bearer(tokenConcessionariaSp()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(NOVO_SEM_CONCESSIONARIA.replace(CPF_NOVO, "173.205.080-00")
                                .replace("larissa@email.test", "larissa").replace("\"F\"", "\"X\"")))
                .andExpect(problema(400, "DADOS_INVALIDOS"))
                .andExpect(jsonPath("$.erros[*].campo", containsInAnyOrder("cpf", "email", "sexo")));
    }

    @Test
    @DisplayName("PUT 200 e PATCH 200 em cliente próprio")
    void substituirEAtualizar() throws Exception {
        mvc.perform(put("/clientes/{id}", CLIENTE_CARLA_SP).header(HttpHeaders.AUTHORIZATION, bearer(tokenConcessionariaSp()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"cpf": "123.456.789-09", "nome": "Carla Mendes Silva", "idade": 30, "sexo": "F", "regiao": "Sudeste",
                                 "dataCompra": "2024-01-20", "veiculoId": 4, "perfilCliente": "ESQUECIDO"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Carla Mendes Silva"))
                .andExpect(jsonPath("$.veiculo.id").value(VEICULO_CARLA))
                .andExpect(jsonPath("$.perfilCliente").value("ESQUECIDO"));

        mvc.perform(patch("/clientes/{id}", CLIENTE_CARLA_SP).header(HttpHeaders.AUTHORIZATION, bearer(tokenConcessionariaSp()))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"telefone\": \"(11) 95555-4444\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.telefone").value("(11) 95555-4444"))
                .andExpect(jsonPath("$.nome").value("Carla Mendes Silva"));
    }

    @Test
    @DisplayName("PATCH 403: cliente de outra concessionária")
    void atualizarDeOutraConcessionaria() throws Exception {
        mvc.perform(patch("/clientes/{id}", CLIENTE_DIEGO_RJ).header(HttpHeaders.AUTHORIZATION, bearer(tokenConcessionariaSp()))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"nome\": \"Invasor\"}"))
                .andExpect(problema(403, "ACESSO_NEGADO"));
    }

    @Test
    @DisplayName("DELETE 204: exclusão lógica do cliente; depois 404")
    void excluir() throws Exception {
        mvc.perform(delete("/clientes/{id}", CLIENTE_CARLA_SP).header(HttpHeaders.AUTHORIZATION, bearer(tokenConcessionariaSp())))
                .andExpect(status().isNoContent());
        mvc.perform(get("/clientes/{id}", CLIENTE_CARLA_SP).header(HttpHeaders.AUTHORIZATION, bearer(tokenAdmin())))
                .andExpect(problema(404, "RECURSO_NAO_ENCONTRADO"));
    }
}
