package br.com.ford.vinshare.domain.servico;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DadosListagemServico(
        Long id,
        Long clienteId,
        Long veiculoId,
        Long concessionariaId,
        String tipoServico,
        LocalDate dataServico,
        BigDecimal valorServico,
        StatusServico statusServico
) {
    public DadosListagemServico(Servico servico) {
        this(
                servico.getId(),
                servico.getCliente().getId(),
                servico.getVeiculo().getId(),
                servico.getConcessionaria().getId(),
                servico.getTipoServico(),
                servico.getDataServico(),
                servico.getValorServico(),
                servico.getStatusServico()
        );
    }
}
