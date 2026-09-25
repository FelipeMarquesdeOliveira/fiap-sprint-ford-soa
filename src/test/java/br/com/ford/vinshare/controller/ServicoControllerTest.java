package br.com.ford.vinshare.controller;

import br.com.ford.vinshare.support.IntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.time.LocalDate;

import static br.com.ford.vinshare.support.DadosDemo.*;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("Serviços - /servicos (regras de negócio e escopo)")
class ServicoControllerTest extends IntegrationTest {

    private static String servico(long clienteId, long veiculoId, String status, LocalDate data) {
        return """
                {"clienteId": %d, "veiculoId": %d, "tipoServico": "Revisão 20.000 km", "dataServico": "%s",
                 "valorServico": 1290.00, "garantiaAtiva": true, "statusServico": "%s"}
                """.formatted(clienteId, veiculoId, data, status);
    }

    @Test
    @DisplayName("GET 200: ADMIN vê todos; CONCESSIONARIA apenas os que executou")
    void listarComEscopo() throws Exception {
        mvc.perform(get("/servicos").header(HttpHeaders.AUTHORIZATION, bearer(tokenAdmin())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page.totalElements").value(15));

        mvc.perform(get("/servicos").header(HttpHeaders.AUTHORIZATION, bearer(tokenConcessionariaRj())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page.totalElements").value(4))
                .andExpect(jsonPath("$.content[*].concessionariaId", everyItem(is((int) CONCESSIONARIA_CARIOCA))));
    }

    @Test
    @DisplayName("GET 200: filtro por status via query string; 400 para status inexistente")
    void filtrarPorStatus() throws Exception {
        mvc.perform(get("/servicos").param("status", "AGENDADO").header(HttpHeaders.AUTHORIZATION, bearer(tokenAnalista())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id").value(SERVICO_AGENDADO_RJ));

        mvc.perform(get("/servicos").param("status", "XPTO").header(HttpHeaders.AUTHORIZATION, bearer(tokenAnalista())))
                .andExpect(problema(400, "REQUISICAO_INVALIDA"));
    }

    @Test
    @DisplayName("GET 403: serviço executado por outra concessionária")
    void detalharDeOutraConcessionaria() throws Exception {
        mvc.perform(get("/servicos/{id}", SERVICO_CONCLUIDO_SP).header(HttpHeaders.AUTHORIZATION, bearer(tokenConcessionariaRj())))
                .andExpect(problema(403, "ACESSO_NEGADO"));
    }

    @Test
    @DisplayName("POST 201: concessionária registra serviço para o próprio cliente")
    void registrar() throws Exception {
        mvc.perform(post("/servicos").header(HttpHeaders.AUTHORIZATION, bearer(tokenConcessionariaSp()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(servico(CLIENTE_CARLA_SP, VEICULO_CARLA, "CONCLUIDO", LocalDate.now().minusDays(1))))
                .andExpect(status().isCreated())
                .andExpect(header().string(HttpHeaders.LOCATION, matchesPattern(".*/servicos/\\d+$")))
                .andExpect(jsonPath("$.concessionaria.id").value(CONCESSIONARIA_PAULISTA))
                .andExpect(jsonPath("$.cliente.id").value(CLIENTE_CARLA_SP))
                .andExpect(jsonPath("$.statusServico").value("CONCLUIDO"));
    }

    @Test
    @DisplayName("POST 422: veículo que não pertence ao cliente")
    void veiculoDeOutroCliente() throws Exception {
        mvc.perform(post("/servicos").header(HttpHeaders.AUTHORIZATION, bearer(tokenConcessionariaSp()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(servico(CLIENTE_CARLA_SP, VEICULO_ANA, "AGENDADO", LocalDate.now().plusDays(5))))
                .andExpect(problema(422, "REGRA_DE_NEGOCIO"))
                .andExpect(jsonPath("$.detail", containsString("não pertence ao cliente")));
    }

    @Test
    @DisplayName("POST 422: serviço CONCLUIDO com data futura")
    void concluidoNoFuturo() throws Exception {
        mvc.perform(post("/servicos").header(HttpHeaders.AUTHORIZATION, bearer(tokenConcessionariaSp()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(servico(CLIENTE_CARLA_SP, VEICULO_CARLA, "CONCLUIDO", LocalDate.now().plusDays(10))))
                .andExpect(problema(422, "REGRA_DE_NEGOCIO"));
    }

    @Test
    @DisplayName("POST 400: campos obrigatórios ausentes e valor negativo")
    void dadosInvalidos() throws Exception {
        mvc.perform(post("/servicos").header(HttpHeaders.AUTHORIZATION, bearer(tokenAdmin()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"valorServico\": -10}"))
                .andExpect(problema(400, "DADOS_INVALIDOS"))
                .andExpect(jsonPath("$.erros[*].campo",
                        hasItems("clienteId", "veiculoId", "tipoServico", "dataServico", "valorServico", "statusServico")));
    }

    @Test
    @DisplayName("PATCH 200: agendamento é concluído; depois o serviço finalizado não aceita alterações (422)")
    void concluirAgendamento() throws Exception {
        mvc.perform(patch("/servicos/{id}", SERVICO_AGENDADO_RJ).header(HttpHeaders.AUTHORIZATION, bearer(tokenConcessionariaRj()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"statusServico\": \"CONCLUIDO\", \"dataServico\": \"%s\"}".formatted(LocalDate.now())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusServico").value("CONCLUIDO"));

        mvc.perform(patch("/servicos/{id}", SERVICO_AGENDADO_RJ).header(HttpHeaders.AUTHORIZATION, bearer(tokenConcessionariaRj()))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"valorServico\": 1.00}"))
                .andExpect(problema(422, "REGRA_DE_NEGOCIO"));
    }

    @Test
    @DisplayName("PUT 422: serviço CANCELADO é imutável")
    void substituirFinalizado() throws Exception {
        mvc.perform(put("/servicos/{id}", SERVICO_CANCELADO_RS).header(HttpHeaders.AUTHORIZATION, bearer(tokenAdmin()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(servico(10, 10, "AGENDADO", LocalDate.now().plusDays(3))
                                .replace("\"statusServico\"", "\"concessionariaId\": 4, \"statusServico\"")))
                .andExpect(problema(422, "REGRA_DE_NEGOCIO"));
    }

    @Test
    @DisplayName("DELETE 204 (ADMIN) e 404 para serviço inexistente")
    void excluir() throws Exception {
        mvc.perform(delete("/servicos/{id}", SERVICO_CANCELADO_RS).header(HttpHeaders.AUTHORIZATION, bearer(tokenAdmin())))
                .andExpect(status().isNoContent());
        mvc.perform(delete("/servicos/{id}", SERVICO_CANCELADO_RS).header(HttpHeaders.AUTHORIZATION, bearer(tokenAdmin())))
                .andExpect(problema(404, "RECURSO_NAO_ENCONTRADO"));
    }
}
