package br.com.ford.vinshare.domain.cliente;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

/** Atualização parcial (PATCH): apenas os campos não nulos são alterados. */
public record DadosAtualizacaoCliente(
        @Size(min = 1, max = 255, message = "Nome deve ter entre 1 e 255 caracteres")
        String nome,

        @Schema(example = "novo.email@email.test")
        @Email(message = "E-mail inválido")
        String email,

        @Pattern(regexp = "^\\(?\\d{2}\\)?\\s?9?\\d{4}-?\\d{4}$", message = "Telefone deve estar no formato (11) 91234-5678")
        String telefone,

        @Min(value = 18, message = "Idade mínima é 18 anos")
        @Max(value = 120, message = "Idade inválida")
        Integer idade,

        @Pattern(regexp = "^[MFO]$", message = "Sexo deve ser M, F ou O")
        String sexo,

        @Size(max = 100, message = "Região deve ter no máximo 100 caracteres")
        String regiao,

        @Schema(example = "ECONOMICO")
        PerfilCliente perfilCliente
) {}
