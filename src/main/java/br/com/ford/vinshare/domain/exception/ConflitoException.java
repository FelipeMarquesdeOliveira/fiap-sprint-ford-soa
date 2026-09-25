package br.com.ford.vinshare.domain.exception;

/**
 * Conflito com o estado atual do recurso (duplicidade ou recurso em uso). Traduzido para HTTP 409.
 */
public class ConflitoException extends RuntimeException {
    public ConflitoException(String mensagem) {
        super(mensagem);
    }
}
