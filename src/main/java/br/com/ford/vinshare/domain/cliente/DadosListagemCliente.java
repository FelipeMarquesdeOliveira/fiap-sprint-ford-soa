package br.com.ford.vinshare.domain.cliente;

import br.com.ford.vinshare.domain.veiculo.DadosListagemVeiculo;

public record DadosListagemCliente(
        Long id,
        String cpf,
        String nome,
        String perfilCliente,
        String regiao,
        DadosListagemVeiculo veiculo
) {
    public DadosListagemCliente(Cliente cliente) {
        this(
                cliente.getId(),
                cliente.getCpf(),
                cliente.getNome(),
                cliente.getPerfilCliente(),
                cliente.getRegiao(),
                cliente.getVeiculo() != null ? new DadosListagemVeiculo(cliente.getVeiculo()) : null
        );
    }
}