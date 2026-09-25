package br.com.ford.vinshare.domain.veiculo;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VeiculoRepository extends JpaRepository<Veiculo, Long> {

    Page<Veiculo> findAllByModeloContainingIgnoreCase(String modelo, Pageable paginacao);

    boolean existsByVin(String vin);

    boolean existsByVinAndIdNot(String vin, Long id);
}
