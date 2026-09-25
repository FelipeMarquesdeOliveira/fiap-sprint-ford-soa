package br.com.ford.vinshare.service;

import br.com.ford.vinshare.domain.cliente.*;
import br.com.ford.vinshare.domain.exception.AcessoNegadoException;
import br.com.ford.vinshare.domain.exception.ConflitoException;
import br.com.ford.vinshare.domain.exception.RecursoNaoEncontradoException;
import br.com.ford.vinshare.domain.exception.RegraDeNegocioException;
import br.com.ford.vinshare.domain.usuario.Usuario;
import br.com.ford.vinshare.domain.veiculo.Veiculo;
import br.com.ford.vinshare.domain.veiculo.VeiculoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Clientes contêm dados pessoais (LGPD): usuários do perfil CONCESSIONARIA só enxergam e
 * alteram clientes da própria concessionária, identificada pelo claim do token.
 */
@Service
@RequiredArgsConstructor
public class ClienteService {

    private final ClienteRepository repository;
    private final VeiculoRepository veiculoRepository;
    private final EscopoConcessionaria escopo;

    @Transactional(readOnly = true)
    public Page<DadosListagemCliente> listar(Usuario usuario, Pageable paginacao) {
        var pagina = usuario.isConcessionaria()
                ? repository.findAllByAtivoTrueAndConcessionaria_Id(usuario.getConcessionariaId(), paginacao)
                : repository.findAllByAtivoTrue(paginacao);
        return pagina.map(DadosListagemCliente::new);
    }

    @Transactional(readOnly = true)
    public DadosDetalhamentoCliente detalhar(Long id, Usuario usuario) {
        return new DadosDetalhamentoCliente(buscarComAcesso(id, usuario));
    }

    @Transactional
    public DadosDetalhamentoCliente cadastrar(DadosCadastroCliente dados, Usuario usuario) {
        var concessionaria = escopo.resolver(dados.concessionariaId(), usuario, "clientes");
        if (repository.existsByCpf(dados.cpf())) {
            throw new ConflitoException("Já existe um cliente cadastrado com o CPF informado.");
        }
        var veiculo = buscarVeiculoDisponivel(dados.veiculoId(), null);
        var cliente = repository.save(new Cliente(dados, veiculo, concessionaria));
        return new DadosDetalhamentoCliente(cliente);
    }

    @Transactional
    public DadosDetalhamentoCliente substituir(Long id, DadosCadastroCliente dados, Usuario usuario) {
        var cliente = buscarComAcesso(id, usuario);
        var concessionaria = escopo.resolver(dados.concessionariaId(), usuario, "clientes");
        if (repository.existsByCpfAndIdNot(dados.cpf(), id)) {
            throw new ConflitoException("Já existe outro cliente cadastrado com o CPF informado.");
        }
        var veiculo = buscarVeiculoDisponivel(dados.veiculoId(), cliente);
        cliente.substituirDados(dados, veiculo, concessionaria);
        return new DadosDetalhamentoCliente(cliente);
    }

    @Transactional
    public DadosDetalhamentoCliente atualizar(Long id, DadosAtualizacaoCliente dados, Usuario usuario) {
        var cliente = buscarComAcesso(id, usuario);
        cliente.atualizarInformacoes(dados);
        return new DadosDetalhamentoCliente(cliente);
    }

    /** Exclusão lógica (preserva o histórico de serviços para o cálculo de VIN Share). */
    @Transactional
    public void excluir(Long id, Usuario usuario) {
        buscarComAcesso(id, usuario).excluir();
    }

    private Cliente buscarComAcesso(Long id, Usuario usuario) {
        var cliente = repository.findByIdAndAtivoTrue(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente " + id + " não encontrado."));
        if (!usuario.podeAcessarConcessionaria(cliente.getConcessionariaId())) {
            throw new AcessoNegadoException("Este cliente pertence a outra concessionária.");
        }
        return cliente;
    }

    /** Um veículo pertence a no máximo um cliente ativo. */
    private Veiculo buscarVeiculoDisponivel(Long veiculoId, Cliente clienteAtual) {
        if (veiculoId == null) {
            return null;
        }
        var veiculo = veiculoRepository.findById(veiculoId)
                .orElseThrow(() -> new RegraDeNegocioException("Veículo " + veiculoId + " não existe."));
        boolean mesmoVeiculo = clienteAtual != null && clienteAtual.getVeiculo() != null
                && clienteAtual.getVeiculo().getId().equals(veiculoId);
        if (!mesmoVeiculo && repository.existsByVeiculoIdAndAtivoTrue(veiculoId)) {
            throw new ConflitoException("O veículo " + veiculoId + " já está vinculado a outro cliente.");
        }
        return veiculo;
    }
}
