package br.com.ford.vinshare.domain.servico;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Representação completa usada no POST e no PUT. */
public record DadosCadastroServico(
        @Schema(example = "1")
        @NotNull(message = "Cliente é obrigatório")
        Long clienteId,

        @Schema(example = "1", description = "Deve ser o veículo do cliente")
        @NotNull(message = "Veículo é obrigatório")
        Long veiculoId,

        @Schema(example = "1", description = "Concessionária que executa o serviço. Obrigatório para ADMIN; para o perfil CONCESSIONARIA é obtido do token.")
        Long concessionariaId,

        @Schema(example = "Revisão 40.000 km")
        @NotBlank(message = "Tipo de serviço é obrigatório")
        @Size(max = 100, message = "Tipo de serviço deve ter no máximo 100 caracteres")
        String tipoServico,

        @Schema(example = "2026-09-20")
        @NotNull(message = "Data do serviço é obrigatória")
        LocalDate dataServico,

        @Schema(example = "1490.00")
        @NotNull(message = "Valor do serviço é obrigatório")
        @PositiveOrZero(message = "Valor do serviço não pode ser negativo")
        @Digits(integer = 8, fraction = 2, message = "Valor do serviço deve ter até 8 dígitos inteiros e 2 decimais")
        BigDecimal valorServico,

        @Schema(example = "true")
        Boolean garantiaAtiva,

        @Schema(example = "CONCLUIDO")
        @NotNull(message = "Status do serviço é obrigatório")
        StatusServico statusServico
) {}
