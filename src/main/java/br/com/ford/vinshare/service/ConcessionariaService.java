package br.com.ford.vinshare.service;

import br.com.ford.vinshare.domain.concessionaria.*;
import br.com.ford.vinshare.domain.exception.ConflitoException;
import br.com.ford.vinshare.domain.exception.RecursoNaoEncontradoException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ConcessionariaService {

    private final ConcessionariaRepository repository;

    @Transactional(readOnly = true)
    public Page<DadosListagemConcessionaria> listar(String estado, Pageable paginacao) {
        var pagina = estado == null || estado.isBlank()
                ? repository.findAllByAtivaTrue(paginacao)
                : repository.findAllByAtivaTrueAndEstadoIgnoreCase(estado, paginacao);
        return pagina.map(DadosListagemConcessionaria::new);
    }

    @Transactional(readOnly = true)
    public DadosDetalhamentoConcessionaria detalhar(Long id) {
        return new DadosDetalhamentoConcessionaria(buscarAtiva(id));
    }

    @Transactional
    public DadosDetalhamentoConcessionaria cadastrar(DadosCadastroConcessionaria dados) {
        if (repository.existsByCnpj(dados.cnpj())) {
            throw new ConflitoException("Já existe uma concessionária cadastrada com o CNPJ " + dados.cnpj() + ".");
        }
        var concessionaria = repository.save(new Concessionaria(dados));
        return new DadosDetalhamentoConcessionaria(concessionaria);
    }

    @Transactional
    public DadosDetalhamentoConcessionaria substituir(Long id, DadosCadastroConcessionaria dados) {
        var concessionaria = buscarAtiva(id);
        if (repository.existsByCnpjAndIdNot(dados.cnpj(), id)) {
            throw new ConflitoException("Já existe outra concessionária cadastrada com o CNPJ " + dados.cnpj() + ".");
        }
        concessionaria.substituirDados(dados);
        return new DadosDetalhamentoConcessionaria(concessionaria);
    }

    @Transactional
    public DadosDetalhamentoConcessionaria atualizar(Long id, DadosAtualizacaoConcessionaria dados) {
        var concessionaria = buscarAtiva(id);
        concessionaria.atualizarInformacoes(dados);
        return new DadosDetalhamentoConcessionaria(concessionaria);
    }

    /** Exclusão lógica: a concessionária deixa de aparecer, mas o histórico de serviços é preservado. */
    @Transactional
    public void excluir(Long id) {
        buscarAtiva(id).excluir();
    }

    private Concessionaria buscarAtiva(Long id) {
        return repository.findByIdAndAtivaTrue(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Concessionária " + id + " não encontrada."));
    }
}
