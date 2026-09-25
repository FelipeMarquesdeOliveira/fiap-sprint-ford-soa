package br.com.ford.vinshare.domain.autenticacao;

import br.com.ford.vinshare.domain.usuario.Perfil;
import br.com.ford.vinshare.domain.usuario.Usuario;
import io.swagger.v3.oas.annotations.media.Schema;

public record DadosTokenJWT(
        @Schema(description = "JWT assinado (HS256) a ser enviado em Authorization: Bearer <token>")
        String accessToken,
        @Schema(example = "Bearer")
        String tokenType,
        @Schema(description = "Validade do access token em segundos", example = "900")
        long expiresIn,
        @Schema(description = "Token opaco de uso único para obter um novo par de tokens em POST /auth/refresh")
        String refreshToken,
        @Schema(description = "Validade do refresh token em segundos", example = "604800")
        long refreshExpiresIn,
        UsuarioAutenticado usuario
) {
    public record UsuarioAutenticado(Long id, String nome, String email, Perfil perfil, Long concessionariaId) {
        public UsuarioAutenticado(Usuario usuario) {
            this(usuario.getId(), usuario.getNome(), usuario.getEmail(), usuario.getPerfil(), usuario.getConcessionariaId());
        }
    }
}
