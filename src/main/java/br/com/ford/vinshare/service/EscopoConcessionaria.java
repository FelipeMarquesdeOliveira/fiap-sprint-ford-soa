package br.com.ford.vinshare.service;

import br.com.ford.vinshare.domain.concessionaria.Concessionaria;
import br.com.ford.vinshare.domain.concessionaria.ConcessionariaRepository;
import br.com.ford.vinshare.domain.exception.AcessoNegadoException;
import br.com.ford.vinshare.domain.exception.RegraDeNegocioException;
import br.com.ford.vinshare.domain.usuario.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Define a concessionária de um registro a partir do usuário autenticado:
 * para o perfil CONCESSIONARIA ela vem sempre do token (nunca do corpo da requisição);
 * para os demais perfis precisa ser informada.
 */
@Component
@RequiredArgsConstructor
public class EscopoConcessionaria {

    private final ConcessionariaRepository repository;

    public Concessionaria resolver(Long informada, Usuario usuario, String recurso) {
        Long id = informada;
        if (usuario.isConcessionaria()) {
            if (informada != null && !informada.equals(usuario.getConcessionariaId())) {
                throw new AcessoNegadoException(
                        "Usuários do perfil CONCESSIONARIA só podem registrar " + recurso + " da própria concessionária.");
            }
            id = usuario.getConcessionariaId();
        } else if (informada == null) {
            throw new RegraDeNegocioException(
                    "O campo concessionariaId é obrigatório para o perfil " + usuario.getPerfil() + ".");
        }
        Long concessionariaId = id;
        return repository.findByIdAndAtivaTrue(concessionariaId)
                .orElseThrow(() -> new RegraDeNegocioException(
                        "Concessionária " + concessionariaId + " não existe ou está inativa."));
    }
}
