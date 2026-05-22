package br.com.ford.vinshare.domain.vinshare;

import java.util.List;

public record DashboardResponse(
        List<VinShareResponse> vinSharePorConcessionaria,
        VinShareResponse vinShareGeral,
        Long totalClientes,
        Long totalVeiculos,
        Long totalServicos,
        Double receitaTotal
) {}