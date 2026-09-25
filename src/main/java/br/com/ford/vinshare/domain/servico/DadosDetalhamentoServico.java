package br.com.ford.vinshare.domain.servico;

import br.com.ford.vinshare.domain.cliente.DadosListagemCliente;
import br.com.ford.vinshare.domain.concessionaria.DadosListagemConcessionaria;
import br.com.ford.vinshare.domain.veiculo.DadosListagemVeiculo;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DadosDetalhamentoServico(
        Long id,
        DadosListagemCliente cliente,
        DadosListagemVeiculo veiculo,
        DadosListagemConcessionaria concessionaria,
        String tipoServico,
        LocalDate dataServico,
        BigDecimal valorServico,
        Boolean garantiaAtiva,
        StatusServico statusServico
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
