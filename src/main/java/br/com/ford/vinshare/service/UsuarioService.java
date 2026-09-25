package br.com.ford.vinshare.service;

import br.com.ford.vinshare.domain.concessionaria.ConcessionariaRepository;
import br.com.ford.vinshare.domain.exception.ConflitoException;
import br.com.ford.vinshare.domain.exception.RecursoNaoEncontradoException;
import br.com.ford.vinshare.domain.exception.RegraDeNegocioException;
import br.com.ford.vinshare.domain.usuario.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository repository;
    private final ConcessionariaRepository concessionariaRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public Page<DadosDetalhamentoUsuario> listar(Pageable paginacao) {
        return repository.findAll(paginacao).map(DadosDetalhamentoUsuario::new);
    }

    @Transactional(readOnly = true)
    public DadosDetalhamentoUsuario detalhar(Long id) {
        return new DadosDetalhamentoUsuario(buscar(id));
    }

    @Transactional
    public DadosDetalhamentoUsuario cadastrar(DadosCadastroUsuario dados) {
        if (repository.existsByEmailIgnoreCase(dados.email())) {
            throw new ConflitoException("Já existe um usuário cadastrado com o e-mail " + dados.email() + ".");
        }
        validarVinculoComConcessionaria(dados.perfil(), dados.concessionariaId());
        var usuario = repository.save(new Usuario(dados, passwordEncoder.encode(dados.senha())));
        return new DadosDetalhamentoUsuario(usuario);
    }

    /**
     * Atualização parcial. Mudanças de perfil, de concessionária ou a desativação incrementam a
     * versão de token do usuário, invalidando na hora os access tokens emitidos anteriormente.
     */
    @Transactional
    public DadosDetalhamentoUsuario atualizar(Long id, DadosAtualizacaoUsuario dados, Usuario logado) {
        var usuario = buscar(id);
        boolean proprioUsuario = usuario.getId().equals(logado.getId());

        if (dados.nome() != null) {
            usuario.atualizarNome(dados.nome());
        }

        if (dados.perfil() != null || dados.concessionariaId() != null) {
            var novoPerfil = dados.perfil() != null ? dados.perfil() : usuario.getPerfil();
            var novaConcessionaria = dados.concessionariaId() != null ? dados.concessionariaId() : usuario.getConcessionariaId();
            if (proprioUsuario && novoPerfil != Perfil.ADMIN) {
                throw new RegraDeNegocioException("Você não pode remover o seu próprio perfil ADMIN.");
            }
            validarVinculoComConcessionaria(novoPerfil, novaConcessionaria);
            usuario.alterarPerfil(novoPerfil, novaConcessionaria);
        }

        if (Boolean.FALSE.equals(dados.ativo())) {
            if (proprioUsuario) {
                throw new RegraDeNegocioException("Você não pode desativar o seu próprio usuário.");
            }
            usuario.desativar();
        } else if (Boolean.TRUE.equals(dados.ativo())) {
            usuario.ativar();
        }
        return new DadosDetalhamentoUsuario(usuario);
    }

    /** Exclusão lógica: o usuário é desativado e seus tokens deixam de ser aceitos. */
    @Transactional
    public void desativar(Long id, Usuario logado) {
        var usuario = buscar(id);
        if (usuario.getId().equals(logado.getId())) {
            throw new RegraDeNegocioException("Você não pode desativar o seu próprio usuário.");
        }
        usuario.desativar();
    }

    private void validarVinculoComConcessionaria(Perfil perfil, Long concessionariaId) {
        if (perfil != Perfil.CONCESSIONARIA) {
            return;
        }
        if (concessionariaId == null) {
            throw new RegraDeNegocioException("Usuários do perfil CONCESSIONARIA precisam de um concessionariaId.");
        }
        if (concessionariaRepository.findByIdAndAtivaTrue(concessionariaId).isEmpty()) {
            throw new RegraDeNegocioException("Concessionária " + concessionariaId + " não existe ou está inativa.");
        }
    }

    private Usuario buscar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário " + id + " não encontrado."));
    }
}
