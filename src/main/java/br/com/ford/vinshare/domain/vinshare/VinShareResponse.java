package br.com.ford.vinshare.domain.vinshare;

public record VinShareResponse(
        Long concessionariaId,
        String concessionariaNome,
        String regiao,
        Long totalVeiculos,
        Long veiculosComServico,
        Long veiculosSemServico,
        Double vinSharePercentual,
        Double receitaTotalServicos
) {}