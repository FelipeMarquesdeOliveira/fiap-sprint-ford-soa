package br.com.ford.vinshare.domain.veiculo;

public record DadosDetalhamentoVeiculo(
        Long id,
        String vin,
        String modelo,
        String versao,
        Integer anoFabricacao,
        Integer anoModelo,
        String cor,
        String combustivel,
        Double valorCompra,
        String tipoVeiculo
) {
    public DadosDetalhamentoVeiculo(Veiculo veiculo) {
        this(
                veiculo.getId(),
                veiculo.getVin(),
                veiculo.getModelo(),
                veiculo.getVersao(),
                veiculo.getAnoFabricacao(),
                veiculo.getAnoModelo(),
                veiculo.getCor(),
                veiculo.getCombustivel(),
                veiculo.getValorCompra(),
                veiculo.getTipoVeiculo()
        );
    }
}