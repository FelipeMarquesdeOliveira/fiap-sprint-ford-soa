package br.com.ford.vinshare.domain.exception;

/**
 * Requisição bem formada que viola uma regra de negócio. Traduzido para HTTP 422.
 */
public class RegraDeNegocioException extends RuntimeException {
    public RegraDeNegocioException(String mensagem) {
        super(mensagem);
    }
}
