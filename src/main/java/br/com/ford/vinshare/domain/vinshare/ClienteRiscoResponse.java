package br.com.ford.vinshare.domain.vinshare;

import br.com.ford.vinshare.domain.cliente.Cliente;

public record ClienteRiscoResponse(
        Long id,
        String cpf,
        String nome,
        String perfilCliente,
        String regiao,
        String modeloVeiculo,
        Long quantidadeServicos,
        Boolean emRisco
) {
    public static ClienteRiscoResponse fromCliente(Cliente cliente, Long quantidadeServicos, Boolean emRisco) {
        return new ClienteRiscoResponse(
                cliente.getId(),
                cliente.getCpf(),
                cliente.getNome(),
                cliente.getPerfilCliente(),
                cliente.getRegiao(),
                cliente.getVeiculo() != null ? cliente.getVeiculo().getModelo() : null,
                quantidadeServicos,
                emRisco
        );
    }
}