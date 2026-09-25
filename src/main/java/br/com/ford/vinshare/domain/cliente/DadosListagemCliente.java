package br.com.ford.vinshare.domain.cliente;

import br.com.ford.vinshare.domain.veiculo.DadosListagemVeiculo;

public record DadosListagemCliente(
        Long id,
        String cpf,
        String nome,
        PerfilCliente perfilCliente,
        String regiao,
        Long concessionariaId,
        DadosListagemVeiculo veiculo
) {
    public DadosListagemCliente(Cliente cliente) {
        this(
                cliente.getId(),
                cliente.getCpf(),
                cliente.getNome(),
                cliente.getPerfilCliente(),
                cliente.getRegiao(),
                cliente.getConcessionariaId(),
                cliente.getVeiculo() != null ? new DadosListagemVeiculo(cliente.getVeiculo()) : null
        );
    }
}
