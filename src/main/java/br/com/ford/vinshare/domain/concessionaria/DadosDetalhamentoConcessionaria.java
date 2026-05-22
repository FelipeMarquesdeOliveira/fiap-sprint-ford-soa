package br.com.ford.vinshare.domain.concessionaria;

public record DadosDetalhamentoConcessionaria(
        Long id,
        String cnpj,
        String nome,
        String regiao,
        String cidade,
        String estado,
        Boolean ativa
) {
    public DadosDetalhamentoConcessionaria(Concessionaria concessionaria) {
        this(
                concessionaria.getId(),
                concessionaria.getCnpj(),
                concessionaria.getNome(),
                concessionaria.getRegiao(),
                concessionaria.getCidade(),
                concessionaria.getEstado(),
                concessionaria.getAtiva()
        );
    }
}