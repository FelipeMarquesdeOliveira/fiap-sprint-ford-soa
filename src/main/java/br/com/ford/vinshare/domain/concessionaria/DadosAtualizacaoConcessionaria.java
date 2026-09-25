package br.com.ford.vinshare.domain.concessionaria;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Atualização parcial (PATCH): apenas os campos não nulos são alterados. CNPJ é imutável. */
public record DadosAtualizacaoConcessionaria(
        @Schema(example = "Ford Paulista Centro - Matriz")
        @Size(min = 1, max = 255, message = "Nome deve ter entre 1 e 255 caracteres")
        String nome,

        @Size(min = 1, max = 100, message = "Região deve ter entre 1 e 100 caracteres")
        String regiao,

        @Size(max = 100, message = "Cidade deve ter no máximo 100 caracteres")
        String cidade,

        @Pattern(regexp = "^[A-Z]{2}$", message = "Estado deve ser a sigla da UF com 2 letras maiúsculas (ex.: SP)")
        String estado
) {}
