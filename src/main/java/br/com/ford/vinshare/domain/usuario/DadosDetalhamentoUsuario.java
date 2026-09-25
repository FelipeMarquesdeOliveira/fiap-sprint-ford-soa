package br.com.ford.vinshare.domain.usuario;

import java.time.LocalDateTime;

/** Nunca expõe a senha (nem o hash). */
public record DadosDetalhamentoUsuario(
        Long id,
        String nome,
        String email,
        Perfil perfil,
        Long concessionariaId,
        Boolean ativo,
        LocalDateTime criadoEm
) {
    public DadosDetalhamentoUsuario(Usuario usuario) {
        this(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getPerfil(),
                usuario.getConcessionariaId(),
                usuario.getAtivo(),
                usuario.getCriadoEm()
        );
    }
}
