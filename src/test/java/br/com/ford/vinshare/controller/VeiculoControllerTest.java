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

@DisplayName("Veículos - /veiculos")
class VeiculoControllerTest extends IntegrationTest {

    private static final String NOVO = """
            {"vin": "9BFZH55L8S8000011", "modelo": "Ranger", "versao": "Raptor 3.0 V6", "anoFabricacao": 2025,
             "anoModelo": 2026, "cor": "Azul", "combustivel": "Gasolina", "valorCompra": 469900.00, "tipoVeiculo": "Picape"}
            """;

    @Test
    @DisplayName("GET 200: lista paginada e filtro por modelo")
    void listar() throws Exception {
        mvc.perform(get("/veiculos").header(HttpHeaders.AUTHORIZATION, bearer(tokenAnalista())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page.totalElements").value(10));

        mvc.perform(get("/veiculos").param("modelo", "ranger").header(HttpHeaders.AUTHORIZATION, bearer(tokenAnalista())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(3)))
                .andExpect(jsonPath("$.content[*].modelo", everyItem(is("Ranger"))));
    }

    @Test
    @DisplayName("GET 200 / 404: detalhe por id")
    void detalhar() throws Exception {
        mvc.perform(get("/veiculos/{id}", VEICULO_ANA).header(HttpHeaders.AUTHORIZATION, bearer(tokenConcessionariaRj())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.vin").value(VIN_ANA))
                .andExpect(jsonPath("$.valorCompra").value(229900.00));

        mvc.perform(get("/veiculos/{id}", 999).header(HttpHeaders.AUTHORIZATION, bearer(tokenAdmin())))
                .andExpect(problema(404, "RECURSO_NAO_ENCONTRADO"));
    }

    @Test
    @DisplayName("GET 200: sub-recurso com o histórico de serviços do veículo (mais recente primeiro)")
    void historicoDeServicos() throws Exception {
        mvc.perform(get("/veiculos/{id}/servicos", VEICULO_ANA).header(HttpHeaders.AUTHORIZATION, bearer(tokenAnalista())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].dataServico").value("2025-08-15"))
                .andExpect(jsonPath("$[2].dataServico").value("2024-08-10"));
    }

    @Test
    @DisplayName("POST 201: perfil CONCESSIONARIA cadastra veículo e recebe Location")
    void cadastrar() throws Exception {
        mvc.perform(post("/veiculos").header(HttpHeaders.AUTHORIZATION, bearer(tokenConcessionariaSp()))
                        .contentType(MediaType.APPLICATION_JSON).content(NOVO))
                .andExpect(status().isCreated())
                .andExpect(header().string(HttpHeaders.LOCATION, matchesPattern(".*/veiculos/\\d+$")))
                .andExpect(jsonPath("$.vin").value(VIN_NOVO))
                .andExpect(jsonPath("$.anoModelo").value(2026));
    }

    @Test
    @DisplayName("POST 409: VIN já cadastrado")
    void vinDuplicado() throws Exception {
        mvc.perform(post("/veiculos").header(HttpHeaders.AUTHORIZATION, bearer(tokenAdmin()))
                        .contentType(MediaType.APPLICATION_JSON).content(NOVO.replace(VIN_NOVO, VIN_ANA)))
                .andExpect(problema(409, "CONFLITO"));
    }

    @Test
    @DisplayName("POST 400: VIN fora do padrão ISO 3779 (contém a letra O) e valor negativo")
    void vinInvalido() throws Exception {
        mvc.perform(post("/veiculos").header(HttpHeaders.AUTHORIZATION, bearer(tokenAdmin()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(NOVO.replace(VIN_NOVO, "9BFZH55L8S80000O1").replace("469900.00", "-1")))
                .andExpect(problema(400, "DADOS_INVALIDOS"))
                .andExpect(jsonPath("$.erros[*].campo", containsInAnyOrder("vin", "valorCompra")));
    }

    @Test
    @DisplayName("PUT 200 e PATCH 200: substituição completa e atualização parcial")
    void substituirEAtualizar() throws Exception {
        mvc.perform(put("/veiculos/{id}", VEICULO_ANA).header(HttpHeaders.AUTHORIZATION, bearer(tokenAdmin()))
                        .contentType(MediaType.APPLICATION_JSON).content(NOVO.replace(VIN_NOVO, VIN_ANA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.versao").value("Raptor 3.0 V6"));

        mvc.perform(patch("/veiculos/{id}", VEICULO_ANA).header(HttpHeaders.AUTHORIZATION, bearer(tokenAdmin()))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"cor\": \"Cinza Carbonizado\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cor").value("Cinza Carbonizado"))
                .andExpect(jsonPath("$.versao").value("Raptor 3.0 V6"))
                .andExpect(jsonPath("$.vin").value(VIN_ANA));
    }

    @Test
    @DisplayName("DELETE 409: veículo com cliente/serviços vinculados não pode ser excluído")
    void excluirVinculado() throws Exception {
        mvc.perform(delete("/veiculos/{id}", VEICULO_ANA).header(HttpHeaders.AUTHORIZATION, bearer(tokenAdmin())))
                .andExpect(problema(409, "CONFLITO"));
    }

    @Test
    @DisplayName("DELETE 204: veículo sem vínculos é removido e passa a responder 404")
    void excluir() throws Exception {
        var location = mvc.perform(post("/veiculos").header(HttpHeaders.AUTHORIZATION, bearer(tokenAdmin()))
                        .contentType(MediaType.APPLICATION_JSON).content(NOVO))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getHeader(HttpHeaders.LOCATION);

        mvc.perform(delete(location).header(HttpHeaders.AUTHORIZATION, bearer(tokenAdmin())))
                .andExpect(status().isNoContent());
        mvc.perform(get(location).header(HttpHeaders.AUTHORIZATION, bearer(tokenAdmin())))
                .andExpect(problema(404, "RECURSO_NAO_ENCONTRADO"));
    }
}
