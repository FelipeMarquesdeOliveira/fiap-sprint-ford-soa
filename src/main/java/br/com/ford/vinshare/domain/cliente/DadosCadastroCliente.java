package br.com.ford.vinshare.domain.cliente;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import org.hibernate.validator.constraints.br.CPF;

import java.time.LocalDate;

/** Representação completa usada no POST e no PUT. */
public record DadosCadastroCliente(
        @Schema(example = "173.205.080-52")
        @NotBlank(message = "CPF é obrigatório")
        @CPF(message = "CPF inválido")
        String cpf,

        @Schema(example = "Larissa Teixeira")
        @NotBlank(message = "Nome é obrigatório")
        @Size(max = 255, message = "Nome deve ter no máximo 255 caracteres")
        String nome,

        @Schema(example = "larissa.teixeira@email.test")
        @Email(message = "E-mail inválido")
        @Size(max = 255, message = "E-mail deve ter no máximo 255 caracteres")
        String email,

        @Schema(example = "(11) 97777-1234")
        @Pattern(regexp = "^\\(?\\d{2}\\)?\\s?9?\\d{4}-?\\d{4}$", message = "Telefone deve estar no formato (11) 91234-5678")
        String telefone,

        @Schema(example = "34")
        @Min(value = 18, message = "Idade mínima é 18 anos")
        @Max(value = 120, message = "Idade inválida")
        Integer idade,

        @Schema(example = "F", description = "M, F ou O (outro)")
        @Pattern(regexp = "^[MFO]$", message = "Sexo deve ser M, F ou O")
        String sexo,

        @Schema(example = "Sudeste")
        @Size(max = 100, message = "Região deve ter no máximo 100 caracteres")
        String regiao,

        @Schema(example = "2025-09-15")
        @PastOrPresent(message = "Data de compra não pode estar no futuro")
        LocalDate dataCompra,

        @Schema(example = "1", description = "Veículo comprado pelo cliente")
        Long veiculoId,

        @Schema(example = "1", description = "Concessionária da venda. Obrigatório para ADMIN; para o perfil CONCESSIONARIA é obtido do token.")
        Long concessionariaId,

        @Schema(example = "FIEL")
        PerfilCliente perfilCliente
) {}
