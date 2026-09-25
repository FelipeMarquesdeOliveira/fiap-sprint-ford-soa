package br.com.ford.vinshare.domain.usuario;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

/** PATCH de usuário. Alterar perfil ou desativar invalida imediatamente os tokens do usuário. */
public record DadosAtualizacaoUsuario(
        @Size(min = 1, max = 100, message = "Nome deve ter entre 1 e 100 caracteres")
        String nome,

        @Schema(example = "ANALISTA")
        Perfil perfil,

        Long concessionariaId,

        @Schema(example = "true")
        Boolean ativo
) {}
