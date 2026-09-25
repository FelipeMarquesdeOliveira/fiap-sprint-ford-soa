package br.com.ford.vinshare.domain.usuario;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

public record DadosCadastroUsuario(
        @Schema(example = "Gestora Ford Mineira")
        @NotBlank(message = "Nome é obrigatório")
        @Size(max = 100, message = "Nome deve ter no máximo 100 caracteres")
        String nome,

        @Schema(example = "concessionaria.mg@vinshare.test")
        @NotBlank(message = "E-mail é obrigatório")
        @Email(message = "E-mail inválido")
        @Size(max = 255, message = "E-mail deve ter no máximo 255 caracteres")
        String email,

        @Schema(example = "Mineira@2026", description = "Mínimo 8 caracteres, com maiúscula, minúscula, número e caractere especial")
        @NotBlank(message = "Senha é obrigatória")
        @Size(max = 72, message = "Senha deve ter no máximo 72 caracteres")
        @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,}$",
                message = "Senha deve ter no mínimo 8 caracteres, com letra maiúscula, minúscula, número e caractere especial")
        String senha,

        @Schema(example = "CONCESSIONARIA")
        @NotNull(message = "Perfil é obrigatório")
        Perfil perfil,

        @Schema(example = "3", description = "Obrigatório quando o perfil é CONCESSIONARIA")
        Long concessionariaId
) {}
