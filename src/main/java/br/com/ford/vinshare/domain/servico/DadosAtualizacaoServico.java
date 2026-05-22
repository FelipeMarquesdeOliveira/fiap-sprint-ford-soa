package br.com.ford.vinshare.domain.servico;

import jakarta.validation.constraints.NotNull;

public record DadosAtualizacaoServico(
        @NotNull(message = "ID é obrigatório")
        Long id,
        String tipoServico,
        String dataServico,
        Double valorServico,
        Boolean garantiaAtiva,
        String statusServico
) {
    public void atualizarInformacoes(Servico servico) {
        if (tipoServico != null && !tipoServico.isBlank()) {
            servico.setTipoServico(tipoServico);
        }
        if (dataServico != null) {
            servico.setDataServico(dataServico);
        }
        if (valorServico != null) {
            servico.setValorServico(valorServico);
        }
        if (garantiaAtiva != null) {
            servico.setGarantiaAtiva(garantiaAtiva);
        }
        if (statusServico != null) {
            servico.setStatusServico(statusServico);
        }
    }
}