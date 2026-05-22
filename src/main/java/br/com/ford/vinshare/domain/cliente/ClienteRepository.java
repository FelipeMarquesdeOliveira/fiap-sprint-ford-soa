package br.com.ford.vinshare.domain.cliente;

import br.com.ford.vinshare.domain.concessionaria.Concessionaria;
import br.com.ford.vinshare.domain.veiculo.Veiculo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    Page<Cliente> findAllByAtivoTrue(Pageable paginacao);
    Optional<Cliente> findByCpf(String cpf);
    List<Cliente> findByPerfilCliente(String perfilCliente);
    List<Cliente> findByConcessionariaId(Long concessionariaId);
}