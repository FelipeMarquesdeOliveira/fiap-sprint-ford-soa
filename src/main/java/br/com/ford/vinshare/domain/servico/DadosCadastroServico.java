package br.com.ford.vinshare.domain.servico;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DadosCadastroServico(
        @NotNull(message = "Cliente ID é obrigatório")
        Long clienteId,
        @NotNull(message = "Veículo ID é obrigatório")
        Long veiculoId,
        @NotNull(message = "Concessionária ID é obrigatória")
        Long concessionariaId,
        @NotBlank(message = "Tipo de serviço é obrigatório")
        String tipoServico,
        String dataServico,
        Double valorServico,
        Boolean garantiaAtiva,
        String statusServico
) {}