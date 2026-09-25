package br.com.ford.vinshare.domain.servico;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ServicoRepository extends JpaRepository<Servico, Long> {

    @Override
    @EntityGraph(attributePaths = {"cliente", "veiculo", "concessionaria"})
    Page<Servico> findAll(Pageable paginacao);

    @EntityGraph(attributePaths = {"cliente", "veiculo", "concessionaria"})
    Page<Servico> findAllByStatusServico(StatusServico status, Pageable paginacao);

    @EntityGraph(attributePaths = {"cliente", "veiculo", "concessionaria"})
    Page<Servico> findAllByConcessionaria_Id(Long concessionariaId, Pageable paginacao);

    @EntityGraph(attributePaths = {"cliente", "veiculo", "concessionaria"})
    Page<Servico> findAllByConcessionaria_IdAndStatusServico(Long concessionariaId, StatusServico status, Pageable paginacao);

    @EntityGraph(attributePaths = {"cliente", "concessionaria"})
    List<Servico> findAllByVeiculoIdOrderByDataServicoDesc(Long veiculoId);

    boolean existsByVeiculoId(Long veiculoId);

    long countByStatusServico(StatusServico status);

    /** Quantidade de serviços com determinado status por cliente: [clienteId, quantidade]. */
    @Query("SELECT s.cliente.id, COUNT(s) FROM Servico s WHERE s.statusServico = :status GROUP BY s.cliente.id")
    List<Object[]> contarPorClienteComStatus(@Param("status") StatusServico status);

    /** Receita de serviços com determinado status por concessionária: [concessionariaId, soma]. */
    @Query("SELECT s.concessionaria.id, SUM(s.valorServico) FROM Servico s WHERE s.statusServico = :status GROUP BY s.concessionaria.id")
    List<Object[]> somarReceitaPorConcessionariaComStatus(@Param("status") StatusServico status);
}
