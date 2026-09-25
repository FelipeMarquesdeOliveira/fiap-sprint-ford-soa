package br.com.ford.vinshare.domain.veiculo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

/** Atualização parcial (PATCH): apenas os campos não nulos são alterados. O VIN é imutável. */
public record DadosAtualizacaoVeiculo(
        @Size(min = 1, max = 100, message = "Modelo deve ter entre 1 e 100 caracteres")
        String modelo,

        @Size(min = 1, max = 100, message = "Versão deve ter entre 1 e 100 caracteres")
        String versao,

        @Min(value = 1990, message = "Ano de fabricação deve ser a partir de 1990")
        @Max(value = 2100, message = "Ano de fabricação inválido")
        Integer anoFabricacao,

        @Min(value = 1990, message = "Ano do modelo deve ser a partir de 1990")
        @Max(value = 2100, message = "Ano do modelo inválido")
        Integer anoModelo,

        @Schema(example = "Cinza Carbonizado")
        @Size(max = 50, message = "Cor deve ter no máximo 50 caracteres")
        String cor,

        @Size(max = 50, message = "Combustível deve ter no máximo 50 caracteres")
        String combustivel,

        @PositiveOrZero(message = "Valor de compra não pode ser negativo")
        @Digits(integer = 10, fraction = 2, message = "Valor de compra deve ter até 10 dígitos inteiros e 2 decimais")
        BigDecimal valorCompra,

        @Size(max = 50, message = "Tipo de veículo deve ter no máximo 50 caracteres")
        String tipoVeiculo
) {}
