package br.com.ford.vinshare.infra.openapi;

import br.com.ford.vinshare.infra.exception.ErroResponse;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.tags.Tag;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

@Configuration
public class OpenApiConfig {

    public static final String SEGURANCA_JWT = "bearer-jwt";
    private static final String PROBLEM_JSON = "application/problem+json";
    private static final String SCHEMA_PROBLEMA = "#/components/schemas/Problema";

    private static final String DESCRICAO = """
            API RESTful (nível 2 de maturidade de Richardson) para gestão e análise do **VIN Share** \
            na rede de concessionárias Ford - Desafio 02 do Ford FIAP 2026.

            ### Autenticação
            1. Faça login em `POST /auth/login` (ex.: `admin@vinshare.test` / `Admin@123`).
            2. Clique em **Authorize** e cole o `accessToken` retornado.
            3. O access token expira em 15 minutos; use `POST /auth/refresh` com o `refreshToken` para obter um novo par.

            ### Perfis de acesso
            | Perfil | Escopo |
            |---|---|
            | `ADMIN` | Acesso total, gestão de usuários e da rede de concessionárias |
            | `ANALISTA` | Leitura de toda a rede e indicadores de VIN Share |
            | `CONCESSIONARIA` | Clientes e serviços apenas da própria concessionária (id vindo do token) |

            ### Erros
            Todas as respostas de erro seguem o padrão **Problem Details (RFC 9457)** com `application/problem+json` \
            e os campos extras `codigo`, `timestamp` e `erros` (validação).
            """;

    @Bean
    public OpenAPI vinShareOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("VIN Share API - Ford FIAP 2026")
                        .version("3.0.0")
                        .description(DESCRICAO)
                        .contact(new Contact()
                                .name("Felipe Marques de Oliveira e Gabriel Barros Cisoto")
                                .url("https://github.com/FelipeMarquesdeOliveira/fiap-sprint-ford-soa"))
                        .license(new License().name("Uso acadêmico - FIAP")))
                .components(new Components()
                        .addSecuritySchemes(SEGURANCA_JWT, new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Access token obtido em POST /auth/login")))
                .addSecurityItem(new SecurityRequirement().addList(SEGURANCA_JWT))
                .tags(List.of(
                        new Tag().name("Autenticação").description("Login, renovação de token e logout"),
                        new Tag().name("Usuários").description("Gestão de usuários e perfis (ADMIN)"),
                        new Tag().name("Concessionárias").description("Rede de concessionárias Ford (leitura pública)"),
                        new Tag().name("Veículos").description("Veículos Ford identificados pelo VIN"),
                        new Tag().name("Clientes").description("Clientes da rede (escopo por concessionária)"),
                        new Tag().name("Serviços").description("Serviços de manutenção realizados na rede oficial"),
                        new Tag().name("VIN Share").description("Indicadores de retenção no pós-venda"),
                        new Tag().name("Health Check").description("Disponibilidade da API")));
    }

    /**
     * Acrescenta a todas as operações as respostas de erro padronizadas, evitando repetir
     * anotações em cada endpoint. Operações públicas (security vazio) não recebem 401/403.
     */
    @Bean
    public OpenApiCustomizer respostasDeErroPadrao() {
        return openApi -> {
            Map<String, Schema> schemas = ModelConverters.getInstance().read(ErroResponse.class);
            schemas.forEach((nome, schema) -> openApi.getComponents().addSchemas(nome, schema));

            openApi.getPaths().values().forEach(pathItem -> pathItem.readOperations().forEach(operacao -> {
                boolean publica = operacao.getSecurity() != null && operacao.getSecurity().isEmpty();
                ApiResponses respostas = operacao.getResponses();

                if (operacao.getRequestBody() != null || possuiParametros(operacao)) {
                    adicionar(respostas, "400", "Requisição inválida (validação, JSON malformado ou parâmetro com tipo incorreto)");
                }
                if (!publica) {
                    adicionar(respostas, "401", "Token ausente, inválido, expirado ou revogado");
                    adicionar(respostas, "403", "Perfil sem permissão para o recurso");
                }
                adicionar(respostas, "500", "Erro interno inesperado");

                // Toda resposta 4xx/5xx (inclusive as declaradas nos controllers) usa o schema Problema
                respostas.forEach((status, resposta) -> {
                    if (status.startsWith("4") || status.startsWith("5")) {
                        resposta.setContent(conteudoProblema());
                    }
                });
            }));
        };
    }

    private boolean possuiParametros(Operation operacao) {
        return operacao.getParameters() != null && !operacao.getParameters().isEmpty();
    }

    private void adicionar(ApiResponses respostas, String status, String descricao) {
        if (!respostas.containsKey(status)) {
            respostas.addApiResponse(status, erro(descricao));
        }
    }

    private ApiResponse erro(String descricao) {
        return new ApiResponse().description(descricao).content(conteudoProblema());
    }

    private Content conteudoProblema() {
        return new Content().addMediaType(PROBLEM_JSON, new MediaType().schema(new Schema<>().$ref(SCHEMA_PROBLEMA)));
    }
}
