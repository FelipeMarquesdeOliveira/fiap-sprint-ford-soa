package br.com.ford.vinshare.domain.concessionaria;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.br.CNPJ;

/** Representação completa usada no POST e no PUT. */
public record DadosCadastroConcessionaria(
        @Schema(example = "55.666.777/0001-81")
        @NotBlank(message = "CNPJ é obrigatório")
        @CNPJ(message = "CNPJ inválido")
        String cnpj,

        @Schema(example = "Ford Capixaba Vitória")
        @NotBlank(message = "Nome é obrigatório")
        @Size(max = 255, message = "Nome deve ter no máximo 255 caracteres")
        String nome,

        @Schema(example = "Sudeste")
        @NotBlank(message = "Região é obrigatória")
        @Size(max = 100, message = "Região deve ter no máximo 100 caracteres")
        String regiao,

        @Schema(example = "Vitória")
        @Size(max = 100, message = "Cidade deve ter no máximo 100 caracteres")
        String cidade,

        @Schema(example = "ES")
        @Pattern(regexp = "^[A-Z]{2}$", message = "Estado deve ser a sigla da UF com 2 letras maiúsculas (ex.: SP)")
        String estado
) {}
