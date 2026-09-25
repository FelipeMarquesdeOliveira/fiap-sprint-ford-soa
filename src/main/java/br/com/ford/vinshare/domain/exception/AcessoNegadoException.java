package br.com.ford.vinshare.domain.exception;

/**
 * Usuário autenticado sem permissão sobre o recurso (ex.: dado de outra concessionária). Traduzido para HTTP 403.
 */
public class AcessoNegadoException extends RuntimeException {
    public AcessoNegadoException(String mensagem) {
        super(mensagem);
    }
}
