package br.com.ford.vinshare.documentacao;

import br.com.ford.vinshare.support.IntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("Documentação OpenAPI / Swagger")
class OpenApiDocumentacaoTest extends IntegrationTest {

    @Test
    @DisplayName("200: contrato OpenAPI 3 publicado em /api-docs com segurança JWT e erros padronizados")
    void contratoOpenApi() throws Exception {
        var json = mvc.perform(get("/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.openapi", startsWith("3.")))
                .andExpect(jsonPath("$.info.title").value("VIN Share API - Ford FIAP 2026"))
                .andExpect(jsonPath("$.components.securitySchemes.bearer-jwt.scheme").value("bearer"))
                .andExpect(jsonPath("$.components.securitySchemes.bearer-jwt.bearerFormat").value("JWT"))
                .andExpect(jsonPath("$.components.schemas.Problema").exists())
                // endpoints públicos não exigem o cadeado; protegidos documentam 401/403
                .andExpect(jsonPath("$.paths['/auth/login'].post.security", hasSize(0)))
                .andExpect(jsonPath("$.paths['/concessionarias'].get.security", hasSize(0)))
                .andExpect(jsonPath("$.paths['/clientes'].post.responses", allOf(
                        hasKey("201"), hasKey("400"), hasKey("401"), hasKey("403"), hasKey("409"), hasKey("422"))))
                .andExpect(jsonPath("$.paths['/clientes/{id}'].delete.responses", hasKey("204")))
                .andExpect(jsonPath("$.paths['/clientes/{id}']", allOf(hasKey("get"), hasKey("put"), hasKey("patch"), hasKey("delete"))))
                .andExpect(jsonPath("$.paths['/clientes'].post.responses['409'].content['application/problem+json']").exists())
                .andReturn().getResponse().getContentAsString();

        // Exporta o contrato para versionamento em docs/openapi.json
        Files.createDirectories(Path.of("target"));
        Files.writeString(Path.of("target/openapi.json"), json);
    }

    @Test
    @DisplayName("200: Swagger UI disponível sem autenticação")
    void swaggerUi() throws Exception {
        mvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("swagger-ui")));
    }
}
