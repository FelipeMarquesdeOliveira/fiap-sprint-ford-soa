package br.com.ford.vinshare.service;

import br.com.ford.vinshare.domain.cliente.ClienteRepository;
import br.com.ford.vinshare.domain.exception.ConflitoException;
import br.com.ford.vinshare.domain.exception.RecursoNaoEncontradoException;
import br.com.ford.vinshare.domain.servico.DadosListagemServico;
import br.com.ford.vinshare.domain.servico.ServicoRepository;
import br.com.ford.vinshare.domain.veiculo.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Veículos formam um cadastro único da rede (identificados pelo VIN): qualquer concessionária
 * precisa consultar um VIN para atender o cliente, por isso não há restrição por concessionária.
 */
@Service
@RequiredArgsConstructor
public class VeiculoService {

    private final VeiculoRepository repository;
    private final ClienteRepository clienteRepository;
    private final ServicoRepository servicoRepository;

    @Transactional(readOnly = true)
    public Page<DadosListagemVeiculo> listar(String modelo, Pageable paginacao) {
        var pagina = modelo == null || modelo.isBlank()
                ? repository.findAll(paginacao)
                : repository.findAllByModeloContainingIgnoreCase(modelo, paginacao);
        return pagina.map(DadosListagemVeiculo::new);
    }

    @Transactional(readOnly = true)
    public DadosDetalhamentoVeiculo detalhar(Long id) {
        return new DadosDetalhamentoVeiculo(buscar(id));
    }

    @Transactional(readOnly = true)
    public List<DadosListagemServico> historicoDeServicos(Long id) {
        buscar(id);
        return servicoRepository.findAllByVeiculoIdOrderByDataServicoDesc(id).stream()
                .map(DadosListagemServico::new)
                .toList();
    }

    @Transactional
    public DadosDetalhamentoVeiculo cadastrar(DadosCadastroVeiculo dados) {
        if (repository.existsByVin(dados.vin())) {
            throw new ConflitoException("Já existe um veículo cadastrado com o VIN " + dados.vin() + ".");
        }
        var veiculo = repository.save(new Veiculo(dados));
        return new DadosDetalhamentoVeiculo(veiculo);
    }

    @Transactional
    public DadosDetalhamentoVeiculo substituir(Long id, DadosCadastroVeiculo dados) {
        var veiculo = buscar(id);
        if (repository.existsByVinAndIdNot(dados.vin(), id)) {
            throw new ConflitoException("Já existe outro veículo cadastrado com o VIN " + dados.vin() + ".");
        }
        veiculo.substituirDados(dados);
        return new DadosDetalhamentoVeiculo(veiculo);
    }

    @Transactional
    public DadosDetalhamentoVeiculo atualizar(Long id, DadosAtualizacaoVeiculo dados) {
        var veiculo = buscar(id);
        veiculo.atualizarInformacoes(dados);
        return new DadosDetalhamentoVeiculo(veiculo);
    }

    @Transactional
    public void excluir(Long id) {
        buscar(id);
        if (clienteRepository.existsByVeiculoId(id) || servicoRepository.existsByVeiculoId(id)) {
            throw new ConflitoException("O veículo " + id + " possui cliente ou serviços vinculados e não pode ser excluído.");
        }
        repository.deleteById(id);
    }

    private Veiculo buscar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Veículo " + id + " não encontrado."));
    }
}
