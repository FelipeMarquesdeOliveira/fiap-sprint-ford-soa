package br.com.ford.vinshare.domain.exception;

/**
 * Recurso inexistente (ou inativo). Traduzido para HTTP 404.
 */
public class RecursoNaoEncontradoException extends RuntimeException {
    public RecursoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}
