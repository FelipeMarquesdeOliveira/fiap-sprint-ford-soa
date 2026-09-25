package br.com.ford.vinshare.service;

import br.com.ford.vinshare.domain.cliente.Cliente;
import br.com.ford.vinshare.domain.cliente.ClienteRepository;
import br.com.ford.vinshare.domain.concessionaria.Concessionaria;
import br.com.ford.vinshare.domain.concessionaria.ConcessionariaRepository;
import br.com.ford.vinshare.domain.exception.AcessoNegadoException;
import br.com.ford.vinshare.domain.exception.RecursoNaoEncontradoException;
import br.com.ford.vinshare.domain.servico.ServicoRepository;
import br.com.ford.vinshare.domain.servico.StatusServico;
import br.com.ford.vinshare.domain.usuario.Usuario;
import br.com.ford.vinshare.domain.vinshare.ClienteRiscoResponse;
import br.com.ford.vinshare.domain.vinshare.DashboardResponse;
import br.com.ford.vinshare.domain.vinshare.VinShareResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Indicadores de retenção no pós-venda.
 *
 * <p><b>VIN Share</b> de uma concessionária = veículos vendidos por ela (clientes ativos) que
 * tiveram ao menos um serviço CONCLUIDO na rede oficial / total de veículos vendidos.</p>
 * <p><b>Cliente em risco</b> = cliente com 0 (risco ALTO) ou 1 (risco MEDIO) serviço concluído.</p>
 */
@Service
@RequiredArgsConstructor
public class VinShareService {

    private static final int LIMITE_SERVICOS_RISCO = 1;

    private final ConcessionariaRepository concessionariaRepository;
    private final ClienteRepository clienteRepository;
    private final ServicoRepository servicoRepository;

    @Transactional(readOnly = true)
    public List<VinShareResponse> calcularPorConcessionaria() {
        var base = carregarBase();
        return concessionariaRepository.findAllByAtivaTrueOrderByNome().stream()
                .map(concessionaria -> calcular(concessionaria, base))
                .toList();
    }

    @Transactional(readOnly = true)
    public VinShareResponse calcularDaConcessionaria(Long concessionariaId, Usuario usuario) {
        var concessionaria = concessionariaRepository.findByIdAndAtivaTrue(concessionariaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Concessionária " + concessionariaId + " não encontrada."));
        if (!usuario.podeAcessarConcessionaria(concessionariaId)) {
            throw new AcessoNegadoException("Usuários do perfil CONCESSIONARIA só podem consultar o VIN Share da própria concessionária.");
        }
        return calcular(concessionaria, carregarBase());
    }

    @Transactional(readOnly = true)
    public List<ClienteRiscoResponse> identificarClientesRisco(Long concessionariaId, Usuario usuario) {
        Long filtro = concessionariaId;
        if (usuario.isConcessionaria()) {
            if (concessionariaId != null && !usuario.podeAcessarConcessionaria(concessionariaId)) {
                throw new AcessoNegadoException("Usuários do perfil CONCESSIONARIA só podem consultar clientes da própria concessionária.");
            }
            filtro = usuario.getConcessionariaId();
        }
        Long concessionariaFiltro = filtro;
        var base = carregarBase();
        return base.clientes().stream()
                .filter(cliente -> concessionariaFiltro == null || concessionariaFiltro.equals(cliente.getConcessionariaId()))
                .filter(cliente -> base.servicosConcluidos(cliente) <= LIMITE_SERVICOS_RISCO)
                .map(cliente -> ClienteRiscoResponse.de(cliente, base.servicosConcluidos(cliente)))
                .sorted(Comparator.comparingLong(ClienteRiscoResponse::servicosConcluidos).thenComparing(ClienteRiscoResponse::nome))
                .toList();
    }

    @Transactional(readOnly = true)
    public DashboardResponse gerarDashboard() {
        var base = carregarBase();
        var porConcessionaria = concessionariaRepository.findAllByAtivaTrueOrderByNome().stream()
                .map(concessionaria -> calcular(concessionaria, base))
                .toList();

        long totalVeiculos = porConcessionaria.stream().mapToLong(VinShareResponse::totalVeiculos).sum();
        long comServico = porConcessionaria.stream().mapToLong(VinShareResponse::veiculosComServico).sum();
        BigDecimal receita = porConcessionaria.stream().map(VinShareResponse::receitaTotalServicos)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
        var geral = new VinShareResponse(null, "Rede Ford", "Todas", totalVeiculos, comServico,
                totalVeiculos - comServico, percentual(comServico, totalVeiculos), receita);

        long emRisco = base.clientes().stream()
                .filter(cliente -> base.servicosConcluidos(cliente) <= LIMITE_SERVICOS_RISCO)
                .count();

        return new DashboardResponse(
                geral,
                porConcessionaria,
                clienteRepository.countByAtivoTrue(),
                totalVeiculos,
                servicoRepository.countByStatusServico(StatusServico.CONCLUIDO),
                emRisco,
                receita);
    }

    private VinShareResponse calcular(Concessionaria concessionaria, Base base) {
        var vendidos = base.clientes().stream()
                .filter(cliente -> concessionaria.getId().equals(cliente.getConcessionariaId()))
                .toList();
        long total = vendidos.size();
        long comServico = vendidos.stream().filter(cliente -> base.servicosConcluidos(cliente) > 0).count();
        BigDecimal receita = base.receitaPorConcessionaria().getOrDefault(concessionaria.getId(), BigDecimal.ZERO)
                .setScale(2, RoundingMode.HALF_UP);

        return new VinShareResponse(
                concessionaria.getId(),
                concessionaria.getNome(),
                concessionaria.getRegiao(),
                total,
                comServico,
                total - comServico,
                percentual(comServico, total),
                receita);
    }

    /** Carrega em 3 consultas agregadas os dados necessários (evita N+1 consultas por cliente). */
    private Base carregarBase() {
        var clientes = clienteRepository.findAllByAtivoTrueAndVeiculoIsNotNullAndConcessionariaIsNotNull();

        Map<Long, Long> servicosPorCliente = new HashMap<>();
        servicoRepository.contarPorClienteComStatus(StatusServico.CONCLUIDO)
                .forEach(linha -> servicosPorCliente.put((Long) linha[0], ((Number) linha[1]).longValue()));

        Map<Long, BigDecimal> receitaPorConcessionaria = new HashMap<>();
        servicoRepository.somarReceitaPorConcessionariaComStatus(StatusServico.CONCLUIDO)
                .forEach(linha -> receitaPorConcessionaria.put((Long) linha[0], (BigDecimal) linha[1]));

        return new Base(clientes, servicosPorCliente, receitaPorConcessionaria);
    }

    private static double percentual(long parte, long total) {
        if (total == 0) {
            return 0.0;
        }
        return Math.round(parte * 10000.0 / total) / 100.0;
    }

    private record Base(List<Cliente> clientes,
                        Map<Long, Long> servicosPorCliente,
                        Map<Long, BigDecimal> receitaPorConcessionaria) {

        long servicosConcluidos(Cliente cliente) {
            return servicosPorCliente.getOrDefault(cliente.getId(), 0L);
        }
    }
}
