package br.com.ford.vinshare.domain.concessionaria;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DadosCadastroConcessionaria(
        @NotBlank(message = "CNPJ é obrigatório")
        String cnpj,
        @NotBlank(message = "Nome é obrigatório")
        String nome,
        @NotBlank(message = "Região é obrigatória")
        String regiao,
        String cidade,
        String estado
) {
    public Concessionaria toEntity() {
        return new Concessionaria(
                null,
                this.cnpj,
                this.nome,
                this.regiao,
                this.cidade,
                this.estado,
                true
        );
    }
}