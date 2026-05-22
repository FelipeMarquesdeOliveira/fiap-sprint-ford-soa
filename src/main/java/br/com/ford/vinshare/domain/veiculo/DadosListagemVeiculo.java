package br.com.ford.vinshare.domain.veiculo;

public record DadosListagemVeiculo(
        Long id,
        String vin,
        String modelo,
        String versao,
        Integer anoFabricacao
) {
    public DadosListagemVeiculo(Veiculo veiculo) {
        this(
                veiculo.getId(),
                veiculo.getVin(),
                veiculo.getModelo(),
                veiculo.getVersao(),
                veiculo.getAnoFabricacao()
        );
    }
}