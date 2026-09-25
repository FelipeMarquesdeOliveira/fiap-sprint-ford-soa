package br.com.ford.vinshare.infra.security;

import br.com.ford.vinshare.infra.exception.CodigoErro;
import lombok.Getter;

/** Falha na validação de refresh token (inválido, expirado ou reutilizado). Traduzida para HTTP 401. */
@Getter
public class TokenInvalidoException extends RuntimeException {

    private final CodigoErro codigo;

    public TokenInvalidoException(CodigoErro codigo, String mensagem) {
        super(mensagem);
        this.codigo = codigo;
    }
}
