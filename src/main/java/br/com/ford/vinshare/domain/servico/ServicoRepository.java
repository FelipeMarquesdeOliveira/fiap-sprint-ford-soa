package br.com.ford.vinshare.domain.servico;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServicoRepository extends JpaRepository<Servico, Long> {
    Page<Servico> findAllByAtivoTrue(Pageable paginacao);

    @Query("SELECT s FROM Servico s WHERE s.cliente.id = :clienteId ORDER BY s.dataServico DESC")
    List<Servico> findByClienteId(Long clienteId);

    @Query("SELECT s FROM Servico s WHERE s.concessionaria.id = :concessionariaId")
    List<Servico> findByConcessionariaId(Long concessionariaId);

    @Query("SELECT COUNT(s) FROM Servico s WHERE s.cliente.id = :clienteId")
    Long countByClienteId(Long clienteId);

    @Query("SELECT SUM(s.valorServico) FROM Servico s WHERE s.concessionaria.id = :concessionariaId")
    Double sumValorServicoByConcessionariaId(Long concessionariaId);
}