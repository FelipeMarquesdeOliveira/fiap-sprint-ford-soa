package br.com.ford.vinshare.domain.concessionaria;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ConcessionariaRepository extends JpaRepository<Concessionaria, Long> {

    Page<Concessionaria> findAllByAtivaTrue(Pageable paginacao);

    Page<Concessionaria> findAllByAtivaTrueAndEstadoIgnoreCase(String estado, Pageable paginacao);

    List<Concessionaria> findAllByAtivaTrueOrderByNome();

    Optional<Concessionaria> findByIdAndAtivaTrue(Long id);

    boolean existsByCnpj(String cnpj);

    boolean existsByCnpjAndIdNot(String cnpj, Long id);
}
