package br.com.ford.vinshare.domain.vinshare;

import br.com.ford.vinshare.domain.cliente.Cliente;
import br.com.ford.vinshare.domain.cliente.ClienteRepository;
import br.com.ford.vinshare.domain.concessionaria.Concessionaria;
import br.com.ford.vinshare.domain.concessionaria.ConcessionariaRepository;
import br.com.ford.vinshare.domain.servico.ServicoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VinShareService {

    private final ConcessionariaRepository concessionariaRepository;
    private final ClienteRepository clienteRepository;
    private final ServicoRepository servicoRepository;

    public VinShareResponse calcularVinSharePorConcessionaria(Long concessionariaId) {
        Concessionaria concessionaria = concessionariaRepository.findById(concessionariaId)
                .orElseThrow(() -> new RuntimeException("Concessionária não encontrada: " + concessionariaId));

        List<Cliente> clientes = clienteRepository.findByConcessionariaId(concessionariaId);
        long totalVeiculos = clientes.stream()
                .filter(c -> c.getVeiculo() != null)
                .count();

        Long veiculosComServico = clientes.stream()
                .filter(c -> c.getVeiculo() != null)
                .filter(c -> servicoRepository.countByClienteId(c.getId()) > 0)
                .count();

        Double receitaTotal = servicoRepository.sumValorServicoByConcessionariaId(concessionariaId);
        if (receitaTotal == null) receitaTotal = 0.0;

        double vinSharePercentual = totalVeiculos > 0 ? (veiculosComServico * 100.0 / totalVeiculos) : 0.0;

        return new VinShareResponse(
                concessionariaId,
                concessionaria.getNome(),
                concessionaria.getRegiao(),
                totalVeiculos,
                veiculosComServico,
                totalVeiculos - veiculosComServico,
                Math.round(vinSharePercentual * 100.0) / 100.0,
                receitaTotal
        );
    }

    public List<VinShareResponse> calcularVinShareGeral() {
        return concessionariaRepository.findAllByAtivoTrue(org.springframework.data.domain.Pageable.unpaged()).stream()
                .map(c -> calcularVinSharePorConcessionaria(c.getId()))
                .collect(Collectors.toList());
    }

    public List<ClienteRiscoResponse> identificarClientesRisco(Long concessionariaId) {
        List<Cliente> clientes;

        if (concessionariaId != null) {
            clientes = clienteRepository.findByConcessionariaId(concessionariaId);
        } else {
            clientes = clienteRepository.findAll();
        }

        return clientes.stream()
                .filter(c -> c.getVeiculo() != null)
                .map(c -> {
                    Long servicosCount = servicoRepository.countByClienteId(c.getId());
                    Boolean emRisco = servicosCount <= 1;
                    return ClienteRiscoResponse.fromCliente(c, servicosCount, emRisco);
                })
                .filter(r -> r.emRisco())
                .collect(Collectors.toList());
    }

    public DashboardResponse gerarDashboard() {
        List<VinShareResponse> vinShareList = calcularVinShareGeral();

        VinShareResponse vinShareGeral = new VinShareResponse(
                null,
                "Geral",
                "Todas",
                vinShareList.stream().mapToLong(VinShareResponse::totalVeiculos).sum(),
                vinShareList.stream().mapToLong(VinShareResponse::veiculosComServico).sum(),
                vinShareList.stream().mapToLong(VinShareResponse::veiculosSemServico).sum(),
                0.0,
                vinShareList.stream().mapToDouble(VinShareResponse::receitaTotalServicos).sum()
        );

        double percentualGeral = vinShareGeral.totalVeiculos() > 0
                ? (vinShareGeral.veiculosComServico() * 100.0 / vinShareGeral.totalVeiculos())
                : 0.0;
        vinShareGeral = new VinShareResponse(
                null, "Geral", "Todas",
                vinShareGeral.totalVeiculos(),
                vinShareGeral.veiculosComServico(),
                vinShareGeral.veiculosSemServico(),
                Math.round(percentualGeral * 100.0) / 100.0,
                vinShareGeral.receitaTotalServicos()
        );

        return new DashboardResponse(
                vinShareList,
                vinShareGeral,
                clienteRepository.count(),
                clienteRepository.findAll().stream().filter(c -> c.getVeiculo() != null).count(),
                servicoRepository.count(),
                vinShareGeral.receitaTotalServicos()
        );
    }
}