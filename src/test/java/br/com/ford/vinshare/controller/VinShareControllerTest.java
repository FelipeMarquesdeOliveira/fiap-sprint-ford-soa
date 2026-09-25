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

@DisplayName("VIN Share - /vinshare")
class VinShareControllerTest extends IntegrationTest {

    @Test
    @DisplayName("GET 200: dashboard consolida a rede (7 de 10 veículos voltaram = 70%)")
    void dashboard() throws Exception {
        mvc.perform(get("/vinshare/dashboard").header(HttpHeaders.AUTHORIZATION, bearer(tokenAnalista())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.vinShareGeral.totalVeiculos").value(10))
                .andExpect(jsonPath("$.vinShareGeral.veiculosComServico").value(7))
                .andExpect(jsonPath("$.vinShareGeral.vinSharePercentual").value(70.0))
                .andExpect(jsonPath("$.receitaTotal").value(15720.00))
                .andExpect(jsonPath("$.totalServicosConcluidos").value(13))
                .andExpect(jsonPath("$.clientesEmRisco").value(5))
                .andExpect(jsonPath("$.vinSharePorConcessionaria", hasSize(4)));
    }

    @Test
    @DisplayName("GET 200: VIN Share por concessionária")
    void porConcessionaria() throws Exception {
        mvc.perform(get("/vinshare/concessionarias").header(HttpHeaders.AUTHORIZATION, bearer(tokenAdmin())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.concessionariaId == 3)].vinSharePercentual").value(contains(100.0)))
                .andExpect(jsonPath("$[?(@.concessionariaId == 4)].vinSharePercentual").value(contains(50.0)));
    }

    @Test
    @DisplayName("GET 200 / 403 / 404: CONCESSIONARIA consulta apenas o próprio VIN Share")
    void daConcessionaria() throws Exception {
        mvc.perform(get("/vinshare/concessionarias/{id}", CONCESSIONARIA_PAULISTA).header(HttpHeaders.AUTHORIZATION, bearer(tokenConcessionariaSp())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalVeiculos").value(3))
                .andExpect(jsonPath("$.veiculosComServico").value(2))
                .andExpect(jsonPath("$.vinSharePercentual").value(66.67))
                .andExpect(jsonPath("$.receitaTotalServicos").value(5050.00));

        mvc.perform(get("/vinshare/concessionarias/{id}", CONCESSIONARIA_CARIOCA).header(HttpHeaders.AUTHORIZATION, bearer(tokenConcessionariaSp())))
                .andExpect(problema(403, "ACESSO_NEGADO"));

        mvc.perform(get("/vinshare/concessionarias/{id}", 999).header(HttpHeaders.AUTHORIZATION, bearer(tokenAdmin())))
                .andExpect(problema(404, "RECURSO_NAO_ENCONTRADO"));
    }

    @Test
    @DisplayName("GET 200: clientes em risco com nível e CPF mascarado (LGPD)")
    void clientesEmRisco() throws Exception {
        mvc.perform(get("/vinshare/clientes-risco").header(HttpHeaders.AUTHORIZATION, bearer(tokenAnalista())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(5)))
                .andExpect(jsonPath("$[0].nivelRisco").value("ALTO"))
                .andExpect(jsonPath("$[*].cpfMascarado", everyItem(matchesPattern("^\\d{3}\\.\\*{3}\\.\\*{3}-\\d{2}$"))));

        mvc.perform(get("/vinshare/clientes-risco").header(HttpHeaders.AUTHORIZATION, bearer(tokenConcessionariaSp())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(CLIENTE_CARLA_SP));

        mvc.perform(get("/vinshare/clientes-risco").param("concessionariaId", "2")
                        .header(HttpHeaders.AUTHORIZATION, bearer(tokenConcessionariaSp())))
                .andExpect(problema(403, "ACESSO_NEGADO"));
    }

    @Test
    @DisplayName("201 / 200: fluxo completo - registrar a revisão de um cliente em risco eleva o VIN Share da concessionária para 100%")
    void servicoRegistradoAtualizaIndicador() throws Exception {
        mvc.perform(post("/servicos").header(HttpHeaders.AUTHORIZATION, bearer(tokenConcessionariaSp()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"clienteId": %d, "veiculoId": %d, "tipoServico": "Revisão 10.000 km", "dataServico": "%s",
                                 "valorServico": 950.00, "garantiaAtiva": true, "statusServico": "CONCLUIDO"}
                                """.formatted(CLIENTE_CARLA_SP, VEICULO_CARLA, LocalDate.now())))
                .andExpect(status().isCreated());

        mvc.perform(get("/vinshare/concessionarias/{id}", CONCESSIONARIA_PAULISTA).header(HttpHeaders.AUTHORIZATION, bearer(tokenConcessionariaSp())))
                .andExpect(jsonPath("$.vinSharePercentual").value(100.0))
                .andExpect(jsonPath("$.receitaTotalServicos").value(6000.00));
        mvc.perform(get("/vinshare/clientes-risco").header(HttpHeaders.AUTHORIZATION, bearer(tokenConcessionariaSp())))
                .andExpect(jsonPath("$[0].nivelRisco").value("MEDIO"));
    }
}
