package br.com.ford.vinshare.domain.cliente;

import jakarta.validation.constraints.NotBlank;

public record DadosCadastroCliente(
        @NotBlank(message = "CPF é obrigatório")
        String cpf,
        @NotBlank(message = "Nome é obrigatório")
        String nome,
        String email,
        String telefone,
        Integer idade,
        String sexo,
        String regiao,
        String dataCompra,
        Long veiculoId,
        Long concessionariaId,
        String perfilCliente
) {}