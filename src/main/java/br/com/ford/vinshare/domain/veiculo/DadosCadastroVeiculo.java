package br.com.ford.vinshare.domain.veiculo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

/** Representação completa usada no POST e no PUT. */
public record DadosCadastroVeiculo(
        @Schema(example = "9BFZH55L8S8000011", description = "VIN (ISO 3779): 17 caracteres, sem as letras I, O e Q")
        @NotBlank(message = "VIN é obrigatório")
        @Pattern(regexp = "^[A-HJ-NPR-Z0-9]{17}$", message = "VIN deve ter 17 caracteres alfanuméricos maiúsculos, sem I, O e Q")
        String vin,

        @Schema(example = "Ranger")
        @NotBlank(message = "Modelo é obrigatório")
        @Size(max = 100, message = "Modelo deve ter no máximo 100 caracteres")
        String modelo,

        @Schema(example = "Raptor 3.0 V6")
        @NotBlank(message = "Versão é obrigatória")
        @Size(max = 100, message = "Versão deve ter no máximo 100 caracteres")
        String versao,

        @Schema(example = "2025")
        @Min(value = 1990, message = "Ano de fabricação deve ser a partir de 1990")
        @Max(value = 2100, message = "Ano de fabricação inválido")
        Integer anoFabricacao,

        @Schema(example = "2026")
        @Min(value = 1990, message = "Ano do modelo deve ser a partir de 1990")
        @Max(value = 2100, message = "Ano do modelo inválido")
        Integer anoModelo,

        @Schema(example = "Azul Belize")
        @Size(max = 50, message = "Cor deve ter no máximo 50 caracteres")
        String cor,

        @Schema(example = "Gasolina")
        @Size(max = 50, message = "Combustível deve ter no máximo 50 caracteres")
        String combustivel,

        @Schema(example = "469900.00")
        @PositiveOrZero(message = "Valor de compra não pode ser negativo")
        @Digits(integer = 10, fraction = 2, message = "Valor de compra deve ter até 10 dígitos inteiros e 2 decimais")
        BigDecimal valorCompra,

        @Schema(example = "Picape")
        @Size(max = 50, message = "Tipo de veículo deve ter no máximo 50 caracteres")
        String tipoVeiculo
) {}
