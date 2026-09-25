package br.com.ford.vinshare.domain.servico;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Atualização parcial (PATCH), ex.: avançar o status de AGENDADO para CONCLUIDO. */
public record DadosAtualizacaoServico(
        @Size(min = 1, max = 100, message = "Tipo de serviço deve ter entre 1 e 100 caracteres")
        String tipoServico,

        LocalDate dataServico,

        @PositiveOrZero(message = "Valor do serviço não pode ser negativo")
        @Digits(integer = 8, fraction = 2, message = "Valor do serviço deve ter até 8 dígitos inteiros e 2 decimais")
        BigDecimal valorServico,

        Boolean garantiaAtiva,

        @Schema(example = "CONCLUIDO")
        StatusServico statusServico
) {}
