package br.com.ford.vinshare.domain.concessionaria;

public record DadosListagemConcessionaria(
        Long id,
        String nome,
        String regiao,
        String cidade,
        String estado
) {
    public DadosListagemConcessionaria(Concessionaria concessionaria) {
        this(
                concessionaria.getId(),
                concessionaria.getNome(),
                concessionaria.getRegiao(),
                concessionaria.getCidade(),
                concessionaria.getEstado()
        );
    }
}