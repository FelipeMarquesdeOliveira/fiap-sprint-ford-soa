package br.com.ford.vinshare.controller;

import br.com.ford.vinshare.support.IntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

import static br.com.ford.vinshare.support.DadosDemo.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Matriz de controle de acesso (RBAC). Corpos inválidos ("{}") são usados de propósito nas escritas:
 * quando o perfil tem permissão a resposta é 400 (passou pela autorização e falhou na validação);
 * quando não tem, a resposta é 403 antes de qualquer processamento.
 */
@DisplayName("Autorização por perfil (ADMIN, ANALISTA, CONCESSIONARIA)")
class AutorizacaoPorPerfilTest extends IntegrationTest {

    @ParameterizedTest(name = "{3}: {0} {1} {2}")
    @CsvSource(delimiter = '|', textBlock = """
            # perfil         | método | rota                            | status
            ADMIN            | GET    | /usuarios                       | 200
            ANALISTA         | GET    | /usuarios                       | 403
            CONCESSIONARIA   | GET    | /usuarios                       | 403
            ADMIN            | POST   | /usuarios                       | 400
            ANALISTA         | POST   | /usuarios                       | 403
            CONCESSIONARIA   | DELETE | /usuarios/2                     | 403
            ADMIN            | POST   | /concessionarias                | 400
            ANALISTA         | POST   | /concessionarias                | 403
            CONCESSIONARIA   | POST   | /concessionarias                | 403
            CONCESSIONARIA   | PATCH  | /concessionarias/1              | 403
            ANALISTA         | DELETE | /concessionarias/1              | 403
            ADMIN            | POST   | /veiculos                       | 400
            CONCESSIONARIA   | POST   | /veiculos                       | 400
            ANALISTA         | POST   | /veiculos                       | 403
            CONCESSIONARIA   | DELETE | /veiculos/1                     | 403
            ANALISTA         | GET    | /veiculos                       | 200
            ADMIN            | POST   | /clientes                       | 400
            CONCESSIONARIA   | POST   | /clientes                       | 400
            ANALISTA         | POST   | /clientes                       | 403
            ANALISTA         | PATCH  | /clientes/1                     | 403
            ANALISTA         | GET    | /clientes                       | 200
            CONCESSIONARIA   | POST   | /servicos                       | 400
            ANALISTA         | POST   | /servicos                       | 403
            CONCESSIONARIA   | DELETE | /servicos/1                     | 403
            ANALISTA         | DELETE | /servicos/1                     | 403
            ANALISTA         | GET    | /servicos                       | 200
            ADMIN            | GET    | /vinshare/dashboard             | 200
            ANALISTA         | GET    | /vinshare/dashboard             | 200
            CONCESSIONARIA   | GET    | /vinshare/dashboard             | 403
            ANALISTA         | GET    | /vinshare/concessionarias       | 200
            CONCESSIONARIA   | GET    | /vinshare/concessionarias       | 403
            CONCESSIONARIA   | GET    | /vinshare/concessionarias/1     | 200
            CONCESSIONARIA   | GET    | /vinshare/concessionarias/2     | 403
            CONCESSIONARIA   | GET    | /vinshare/clientes-risco        | 200
            CONCESSIONARIA   | GET    | /usuarios/me                    | 200
            ANALISTA         | GET    | /usuarios/me                    | 200
            """)
    void matrizDePermissoes(String perfil, String metodo, String rota, int statusEsperado) throws Exception {
        var requisicao = request(HttpMethod.valueOf(metodo), rota)
                .header(HttpHeaders.AUTHORIZATION, bearer(tokenPorPerfil(perfil)));
        if (!metodo.equals("GET") && !metodo.equals("DELETE")) {
            requisicao.contentType(MediaType.APPLICATION_JSON).content(metodo.equals("PATCH") ? "{\"nome\": \"X\"}" : "{}");
        }

        mvc.perform(requisicao).andExpect(status().is(statusEsperado));
    }

    private String tokenPorPerfil(String perfil) {
        return switch (perfil) {
            case "ADMIN" -> tokenAdmin();
            case "ANALISTA" -> tokenAnalista();
            case "CONCESSIONARIA" -> tokenDe(CONCESSIONARIA_SP);
            default -> throw new IllegalArgumentException(perfil);
        };
    }
}
