package br.com.ford.vinshare.support;

import br.com.ford.vinshare.domain.usuario.Usuario;
import br.com.ford.vinshare.domain.usuario.UsuarioRepository;
import br.com.ford.vinshare.infra.security.TokenService;
import com.jayway.jsonpath.JsonPath;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Base dos testes de integração: sobe a aplicação completa (filtros de segurança,
 * controllers, services, JPA e Flyway) sobre um H2 em memória com a carga de demonstração.
 * Cada teste roda em uma transação desfeita ao final, mantendo os dados isolados.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public abstract class IntegrationTest {

    @Autowired
    protected MockMvc mvc;

    @Autowired
    protected TokenService tokenService;

    @Autowired
    protected UsuarioRepository usuarioRepository;

    /** Access token de um usuário da carga de demonstração (emitido pelo TokenService real). */
    protected String tokenDe(String email) {
        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(email).orElseThrow();
        return tokenService.gerarAccessToken(usuario).token();
    }

    protected String tokenAdmin() {
        return tokenDe(DadosDemo.ADMIN);
    }

    protected String tokenAnalista() {
        return tokenDe(DadosDemo.ANALISTA);
    }

    protected String tokenConcessionariaSp() {
        return tokenDe(DadosDemo.CONCESSIONARIA_SP);
    }

    protected String tokenConcessionariaRj() {
        return tokenDe(DadosDemo.CONCESSIONARIA_RJ);
    }

    protected static String bearer(String token) {
        return "Bearer " + token;
    }

    /** Faz login pelo endpoint real e devolve o JSON da resposta. */
    protected String login(String email, String senha) throws Exception {
        return mvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "%s", "senha": "%s"}
                                """.formatted(email, senha)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
    }

    protected static String campo(String json, String caminho) {
        return JsonPath.read(json, caminho).toString();
    }

    /** Verifica o contrato padronizado de erro (Problem Details - RFC 9457). */
    protected static ResultMatcher problema(int status, String codigo) {
        return result -> {
            status().is(status).match(result);
            content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON).match(result);
            jsonPath("$.status").value(status).match(result);
            jsonPath("$.codigo").value(codigo).match(result);
            jsonPath("$.title").isNotEmpty().match(result);
            jsonPath("$.detail").isNotEmpty().match(result);
            jsonPath("$.type").value(org.hamcrest.Matchers.endsWith("#" + codigo.toLowerCase())).match(result);
            jsonPath("$.instance").value(result.getRequest().getRequestURI()).match(result);
            jsonPath("$.timestamp").isNotEmpty().match(result);
            jsonPath("$.stack").doesNotExist().match(result);
            jsonPath("$.trace").doesNotExist().match(result);
        };
    }
}
