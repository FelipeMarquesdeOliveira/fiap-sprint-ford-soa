package br.com.ford.vinshare.domain.servico;

import br.com.ford.vinshare.domain.cliente.DadosListagemCliente;
import br.com.ford.vinshare.domain.concessionaria.DadosListagemConcessionaria;
import br.com.ford.vinshare.domain.veiculo.DadosListagemVeiculo;

public record DadosDetalhamentoServico(
        Long id,
        DadosListagemCliente cliente,
        DadosListagemVeiculo veiculo,
        DadosListagemConcessionaria concessionaria,
        String tipoServico,
        String dataServico,
        Double valorServico,
        Boolean garantiaAtiva,
        String statusServico
) {
    public DadosDetalhamentoServico(Servico servico) {
        this(
                servico.getId(),
                new DadosListagemCliente(servico.getCliente()),
                new DadosListagemVeiculo(servico.getVeiculo()),
                new DadosListagemConcessionaria(servico.getConcessionaria()),
                servico.getTipoServico(),
                servico.getDataServico(),
                servico.getValorServico(),
                servico.getGarantiaAtiva(),
                servico.getStatusServico()
        );
    }
}