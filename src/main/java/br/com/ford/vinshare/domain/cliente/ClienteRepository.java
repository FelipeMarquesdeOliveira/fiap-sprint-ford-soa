package br.com.ford.vinshare.domain.cliente;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    @EntityGraph(attributePaths = {"veiculo", "concessionaria"})
    Page<Cliente> findAllByAtivoTrue(Pageable paginacao);

    @EntityGraph(attributePaths = {"veiculo", "concessionaria"})
    Page<Cliente> findAllByAtivoTrueAndConcessionaria_Id(Long concessionariaId, Pageable paginacao);

    /** Base do VIN Share: clientes ativos que compraram um veículo em uma concessionária. */
    @EntityGraph(attributePaths = {"veiculo", "concessionaria"})
    List<Cliente> findAllByAtivoTrueAndVeiculoIsNotNullAndConcessionariaIsNotNull();

    Optional<Cliente> findByIdAndAtivoTrue(Long id);

    boolean existsByCpf(String cpf);

    boolean existsByCpfAndIdNot(String cpf, Long id);

    boolean existsByVeiculoId(Long veiculoId);

    boolean existsByVeiculoIdAndAtivoTrue(Long veiculoId);

    long countByAtivoTrue();
}
