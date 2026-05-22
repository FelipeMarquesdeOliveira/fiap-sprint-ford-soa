package br.com.ford.vinshare.domain.veiculo;

import jakarta.validation.constraints.NotNull;

public record DadosAtualizacaoVeiculo(
        @NotNull(message = "ID é obrigatório")
        Long id,
        String modelo,
        String versao,
        Integer anoFabricacao,
        Integer anoModelo,
        String cor,
        String combustivel,
        Double valorCompra,
        String tipoVeiculo
) {
    public void atualizarInformacoes(Veiculo veiculo) {
        if (modelo != null && !modelo.isBlank()) {
            veiculo.setModelo(modelo);
        }
        if (versao != null && !versao.isBlank()) {
            veiculo.setVersao(versao);
        }
        if (anoFabricacao != null) {
            veiculo.setAnoFabricacao(anoFabricacao);
        }
        if (anoModelo != null) {
            veiculo.setAnoModelo(anoModelo);
        }
        if (cor != null) {
            veiculo.setCor(cor);
        }
        if (combustivel != null) {
            veiculo.setCombustivel(combustivel);
        }
        if (valorCompra != null) {
            veiculo.setValorCompra(valorCompra);
        }
        if (tipoVeiculo != null) {
            veiculo.setTipoVeiculo(tipoVeiculo);
        }
    }
}