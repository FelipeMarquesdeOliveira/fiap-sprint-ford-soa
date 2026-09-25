package br.com.ford.vinshare.controller;

import br.com.ford.vinshare.support.IntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("Padronização das respostas de erro (Problem Details)")
class TratamentoDeErrosTest extends IntegrationTest {

    @Test
    @DisplayName("404: rota inexistente (autenticado) segue o mesmo formato")
    void rotaInexistente() throws Exception {
        mvc.perform(get("/rota-que-nao-existe").header(HttpHeaders.AUTHORIZATION, bearer(tokenAdmin())))
                .andExpect(problema(404, "RECURSO_NAO_ENCONTRADO"));
    }

    @Test
    @DisplayName("405: método não suportado informa os métodos permitidos no header Allow")
    void metodoNaoPermitido() throws Exception {
        mvc.perform(delete("/concessionarias").header(HttpHeaders.AUTHORIZATION, bearer(tokenAdmin())))
                .andExpect(problema(405, "METODO_NAO_PERMITIDO"))
                .andExpect(header().string(HttpHeaders.ALLOW, allOf(containsString("GET"), containsString("POST"))));
    }

    @Test
    @DisplayName("400: parâmetro de rota com tipo inválido")
    void tipoDeParametroInvalido() throws Exception {
        mvc.perform(get("/veiculos/abc").header(HttpHeaders.AUTHORIZATION, bearer(tokenAdmin())))
                .andExpect(problema(400, "REQUISICAO_INVALIDA"))
                .andExpect(jsonPath("$.detail", containsString("'id'")));
    }

    @Test
    @DisplayName("400: valor de enum inválido no corpo indica o campo")
    void enumInvalido() throws Exception {
        mvc.perform(patch("/servicos/8").header(HttpHeaders.AUTHORIZATION, bearer(tokenAdmin()))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"statusServico\": \"FINALIZADO\"}"))
                .andExpect(problema(400, "REQUISICAO_INVALIDA"))
                .andExpect(jsonPath("$.detail", containsString("statusServico")));
    }

    @Test
    @DisplayName("400: ordenação por propriedade inexistente")
    void ordenacaoInvalida() throws Exception {
        mvc.perform(get("/concessionarias").param("sort", "senha"))
                .andExpect(problema(400, "REQUISICAO_INVALIDA"))
                .andExpect(jsonPath("$.detail", containsString("'senha'")));
    }

    @Test
    @DisplayName("200: tamanho de página limitado a 100 itens")
    void tamanhoMaximoDePagina() throws Exception {
        mvc.perform(get("/concessionarias").param("size", "100000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page.size").value(100));
    }

    @Test
    @DisplayName("400: corpo ausente")
    void corpoAusente() throws Exception {
        mvc.perform(post("/veiculos").header(HttpHeaders.AUTHORIZATION, bearer(tokenAdmin())).contentType(MediaType.APPLICATION_JSON))
                .andExpect(problema(400, "REQUISICAO_INVALIDA"));
    }

    @Test
    @DisplayName("400: erros de validação trazem a lista campo/mensagem em português")
    void listaDeErrosDeValidacao() throws Exception {
        mvc.perform(post("/veiculos").header(HttpHeaders.AUTHORIZATION, bearer(tokenAdmin()))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"vin\": \"123\"}"))
                .andExpect(problema(400, "DADOS_INVALIDOS"))
                .andExpect(jsonPath("$.erros", hasSize(3)))
                .andExpect(jsonPath("$.erros[?(@.campo == 'vin')].mensagem").value(contains(startsWith("VIN deve ter 17"))))
                .andExpect(jsonPath("$.erros[?(@.campo == 'modelo')].mensagem").value(contains("Modelo é obrigatório")));
    }
}
