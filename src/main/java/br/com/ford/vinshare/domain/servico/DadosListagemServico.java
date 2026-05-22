package br.com.ford.vinshare.domain.servico;

public record DadosListagemServico(
        Long id,
        String tipoServico,
        String dataServico,
        Double valorServico,
        String statusServico
) {
    public DadosListagemServico(Servico servico) {
        this(
                servico.getId(),
                servico.getTipoServico(),
                servico.getDataServico(),
                servico.getValorServico(),
                servico.getStatusServico()
        );
    }
}