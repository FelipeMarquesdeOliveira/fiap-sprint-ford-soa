package br.com.ford.vinshare.domain.servico;

public class ServicoNotFoundException extends RuntimeException {
    public ServicoNotFoundException(String message) {
        super(message);
    }
}