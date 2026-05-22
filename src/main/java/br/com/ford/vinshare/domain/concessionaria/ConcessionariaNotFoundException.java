package br.com.ford.vinshare.domain.concessionaria;

public class ConcessionariaNotFoundException extends RuntimeException {
    public ConcessionariaNotFoundException(String message) {
        super(message);
    }
}