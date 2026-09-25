package br.com.ford.vinshare.domain.cliente;

import br.com.ford.vinshare.domain.concessionaria.DadosListagemConcessionaria;
import br.com.ford.vinshare.domain.veiculo.DadosListagemVeiculo;

import java.time.LocalDate;

public record DadosDetalhamentoCliente(
        Long id,
        String cpf,
        String nome,
        String email,
        String telefone,
        Integer idade,
        String sexo,
        String regiao,
        LocalDate dataCompra,
        DadosListagemVeiculo veiculo,
        DadosListagemConcessionaria concessionaria,
        PerfilCliente perfilCliente,
        Boolean ativo
) {
    public DadosDetalhamentoCliente(Cliente cliente) {
        this(
                cliente.getId(),
                cliente.getCpf(),
                cliente.getNome(),
                cliente.getEmail(),
                cliente.getTelefone(),
                cliente.getIdade(),
                cliente.getSexo(),
                cliente.getRegiao(),
                cliente.getDataCompra(),
                cliente.getVeiculo() != null ? new DadosListagemVeiculo(cliente.getVeiculo()) : null,
                cliente.getConcessionaria() != null ? new DadosListagemConcessionaria(cliente.getConcessionaria()) : null,
                cliente.getPerfilCliente(),
                cliente.getAtivo()
        );
    }
}
