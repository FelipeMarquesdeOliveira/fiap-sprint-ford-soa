package br.com.ford.vinshare.infra.exception;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * Contrato (apenas para documentação OpenAPI) do corpo de erro retornado pela API:
 * Problem Details (RFC 9457) com as extensões "codigo", "timestamp" e "erros".
 */
@Schema(name = "Problema", description = "Resposta de erro padronizada no formato Problem Details (RFC 9457), media type application/problem+json")
public record ErroResponse(
        @Schema(example = "https://github.com/FelipeMarquesdeOliveira/fiap-sprint-ford-soa/blob/main/docs/ERROS.md#recurso_nao_encontrado")
        String type,
        @Schema(example = "Recurso não encontrado")
        String title,
        @Schema(example = "404")
        int status,
        @Schema(example = "Concessionária 99 não encontrada")
        String detail,
        @Schema(example = "/concessionarias/99")
        String instance,
        @Schema(example = "RECURSO_NAO_ENCONTRADO", description = "Código estável do erro, útil para tratamento no cliente")
        String codigo,
        @Schema(example = "2026-09-25T10:15:30.123Z")
        String timestamp,
        @Schema(description = "Presente apenas em erros de validação (codigo DADOS_INVALIDOS)")
        List<CampoInvalido> erros
) {
    public record CampoInvalido(
            @Schema(example = "cnpj") String campo,
            @Schema(example = "CNPJ inválido") String mensagem
    ) {}
}
