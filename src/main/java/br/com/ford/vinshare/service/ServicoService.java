package br.com.ford.vinshare.service;

import br.com.ford.vinshare.domain.cliente.ClienteRepository;
import br.com.ford.vinshare.domain.concessionaria.Concessionaria;
import br.com.ford.vinshare.domain.exception.AcessoNegadoException;
import br.com.ford.vinshare.domain.exception.RecursoNaoEncontradoException;
import br.com.ford.vinshare.domain.exception.RegraDeNegocioException;
import br.com.ford.vinshare.domain.servico.*;
import br.com.ford.vinshare.domain.usuario.Usuario;
import br.com.ford.vinshare.domain.veiculo.VeiculoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;

/**
 * Serviços de manutenção na rede oficial: são o dado que alimenta o VIN Share.
 * Concessionárias registram e consultam apenas os serviços que executaram.
 */
@Service
@RequiredArgsConstructor
public class ServicoService {

    private final ServicoRepository repository;
    private final ClienteRepository clienteRepository;
    private final VeiculoRepository veiculoRepository;
    private final EscopoConcessionaria escopo;
    private final Clock clock;

    @Transactional(readOnly = true)
    public Page<DadosListagemServico> listar(StatusServico status, Usuario usuario, Pageable paginacao) {
        Page<Servico> pagina;
        if (usuario.isConcessionaria()) {
            pagina = status == null
                    ? repository.findAllByConcessionaria_Id(usuario.getConcessionariaId(), paginacao)
                    : repository.findAllByConcessionaria_IdAndStatusServico(usuario.getConcessionariaId(), status, paginacao);
        } else {
            pagina = status == null ? repository.findAll(paginacao) : repository.findAllByStatusServico(status, paginacao);
        }
        return pagina.map(DadosListagemServico::new);
    }

    @Transactional(readOnly = true)
    public DadosDetalhamentoServico detalhar(Long id, Usuario usuario) {
        return new DadosDetalhamentoServico(buscarComAcesso(id, usuario));
    }

    @Transactional
    public DadosDetalhamentoServico cadastrar(DadosCadastroServico dados, Usuario usuario) {
        var concessionaria = escopo.resolver(dados.concessionariaId(), usuario, "serviços");
        var servico = new Servico();
        preencher(servico, dados, concessionaria);
        return new DadosDetalhamentoServico(repository.save(servico));
    }

    @Transactional
    public DadosDetalhamentoServico substituir(Long id, DadosCadastroServico dados, Usuario usuario) {
        var servico = buscarComAcesso(id, usuario);
        garantirQueNaoEstaFinalizado(servico);
        var concessionaria = escopo.resolver(dados.concessionariaId(), usuario, "serviços");
        preencher(servico, dados, concessionaria);
        return new DadosDetalhamentoServico(servico);
    }

    @Transactional
    public DadosDetalhamentoServico atualizar(Long id, DadosAtualizacaoServico dados, Usuario usuario) {
        var servico = buscarComAcesso(id, usuario);
        garantirQueNaoEstaFinalizado(servico);
        servico.atualizarInformacoes(dados);
        validarDataDeConclusao(servico.getStatusServico(), servico.getDataServico());
        return new DadosDetalhamentoServico(servico);
    }

    @Transactional
    public void excluir(Long id) {
        if (!repository.existsById(id)) {
            throw new RecursoNaoEncontradoException("Serviço " + id + " não encontrado.");
        }
        repository.deleteById(id);
    }

    private void preencher(Servico servico, DadosCadastroServico dados, Concessionaria concessionaria) {
        var cliente = clienteRepository.findByIdAndAtivoTrue(dados.clienteId())
                .orElseThrow(() -> new RegraDeNegocioException("Cliente " + dados.clienteId() + " não existe ou está inativo."));
        var veiculo = veiculoRepository.findById(dados.veiculoId())
                .orElseThrow(() -> new RegraDeNegocioException("Veículo " + dados.veiculoId() + " não existe."));
        if (cliente.getVeiculo() == null || !cliente.getVeiculo().getId().equals(veiculo.getId())) {
            throw new RegraDeNegocioException("O veículo " + veiculo.getId() + " não pertence ao cliente " + cliente.getId() + ".");
        }
        validarDataDeConclusao(dados.statusServico(), dados.dataServico());

        servico.substituirDados(dados, cliente, veiculo, concessionaria);
    }

    private void validarDataDeConclusao(StatusServico status, LocalDate data) {
        if (status == StatusServico.CONCLUIDO && data != null && data.isAfter(LocalDate.now(clock))) {
            throw new RegraDeNegocioException("Um serviço CONCLUIDO não pode ter data futura.");
        }
    }

    private void garantirQueNaoEstaFinalizado(Servico servico) {
        if (servico.getStatusServico() != null && servico.getStatusServico().isFinal()) {
            throw new RegraDeNegocioException("O serviço " + servico.getId() + " está " + servico.getStatusServico()
                    + " e não pode mais ser alterado.");
        }
    }

    private Servico buscarComAcesso(Long id, Usuario usuario) {
        var servico = repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Serviço " + id + " não encontrado."));
        if (!usuario.podeAcessarConcessionaria(servico.getConcessionariaId())) {
            throw new AcessoNegadoException("Este serviço foi realizado por outra concessionária.");
        }
        return servico;
    }
}
