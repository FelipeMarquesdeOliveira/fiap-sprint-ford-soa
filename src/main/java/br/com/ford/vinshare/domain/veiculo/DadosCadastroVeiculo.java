package br.com.ford.vinshare.domain.veiculo;

import jakarta.validation.constraints.NotBlank;

public record DadosCadastroVeiculo(
        @NotBlank(message = "VIN é obrigatório")
        String vin,
        @NotBlank(message = "Modelo é obrigatório")
        String modelo,
        @NotBlank(message = "Versão é obrigatória")
        String versao,
        Integer anoFabricacao,
        Integer anoModelo,
        String cor,
        String combustivel,
        Double valorCompra,
        String tipoVeiculo
) {
    public Veiculo toEntity() {
        return new Veiculo(
                null,
                this.vin,
                this.modelo,
                this.versao,
                this.anoFabricacao,
                this.anoModelo,
                this.cor,
                this.combustivel,
                this.valorCompra,
                this.tipoVeiculo
        );
    }
}