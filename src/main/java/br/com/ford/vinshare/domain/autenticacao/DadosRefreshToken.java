package br.com.ford.vinshare.domain.autenticacao;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DadosRefreshToken(
        @Schema(description = "Refresh token recebido no login ou na última renovação")
        @NotBlank(message = "Refresh token é obrigatório")
        @Size(max = 200, message = "Refresh token inválido")
        String refreshToken
) {}
