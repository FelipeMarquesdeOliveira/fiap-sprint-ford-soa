package br.com.ford.vinshare.domain.cliente;

import jakarta.validation.constraints.NotNull;

public record DadosAtualizacaoCliente(
        @NotNull(message = "ID é obrigatório")
        Long id,
        String nome,
        String email,
        String telefone,
        Integer idade,
        String sexo,
        String regiao,
        String perfilCliente
) {
    public void atualizarInformacoes(Cliente cliente) {
        if (nome != null && !nome.isBlank()) {
            cliente.setNome(nome);
        }
        if (email != null) {
            cliente.setEmail(email);
        }
        if (telefone != null) {
            cliente.setTelefone(telefone);
        }
        if (idade != null) {
            cliente.setIdade(idade);
        }
        if (sexo != null) {
            cliente.setSexo(sexo);
        }
        if (regiao != null) {
            cliente.setRegiao(regiao);
        }
        if (perfilCliente != null) {
            cliente.setPerfilCliente(perfilCliente);
        }
    }
}