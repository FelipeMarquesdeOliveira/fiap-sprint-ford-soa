package br.com.ford.vinshare.domain.veiculo;

public class VeiculoNotFoundException extends RuntimeException {
    public VeiculoNotFoundException(String message) {
        super(message);
    }
}