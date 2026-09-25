package br.com.ford.vinshare.domain.vinshare;

import java.math.BigDecimal;
import java.util.List;

public record DashboardResponse(
        VinShareResponse vinShareGeral,
        List<VinShareResponse> vinSharePorConcessionaria,
        long totalClientes,
        long totalVeiculosVendidos,
        long totalServicosConcluidos,
        long clientesEmRisco,
        BigDecimal receitaTotal
) {}
