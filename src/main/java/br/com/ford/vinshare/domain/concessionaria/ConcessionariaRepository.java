package br.com.ford.vinshare.domain.concessionaria;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConcessionariaRepository extends JpaRepository<Concessionaria, Long> {
    Page<Concessionaria> findAllByAtivoTrue(Pageable paginacao);
    Optional<Concessionaria> findByCnpj(String cnpj);
}