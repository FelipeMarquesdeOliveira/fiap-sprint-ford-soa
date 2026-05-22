package br.com.ford.vinshare.domain.concessionaria;

import jakarta.validation.constraints.NotNull;

public record DadosAtualizacaoConcessionaria(
        @NotNull(message = "ID é obrigatório")
        Long id,
        String nome,
        String regiao,
        String cidade,
        String estado
) {
    public void atualizarInformacoes(Concessionaria concessionaria) {
        if (nome != null && !nome.isBlank()) {
            concessionaria.setNome(nome);
        }
        if (regiao != null && !regiao.isBlank()) {
            concessionaria.setRegiao(regiao);
        }
        if (cidade != null) {
            concessionaria.setCidade(cidade);
        }
        if (estado != null) {
            concessionaria.setEstado(estado);
        }
    }
}