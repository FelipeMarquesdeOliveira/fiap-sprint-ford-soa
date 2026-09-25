package br.com.ford.vinshare.domain.vinshare;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "VIN Share: percentual dos veículos vendidos que retornaram à rede oficial para ao menos um serviço concluído")
public record VinShareResponse(
        Long concessionariaId,
        String concessionariaNome,
        String regiao,
        long totalVeiculos,
        long veiculosComServico,
        long veiculosSemServico,
        double vinSharePercentual,
        BigDecimal receitaTotalServicos
) {}
