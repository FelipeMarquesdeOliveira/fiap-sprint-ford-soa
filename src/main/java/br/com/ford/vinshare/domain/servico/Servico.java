package br.com.ford.vinshare.domain.servico;

import br.com.ford.vinshare.domain.cliente.Cliente;
import br.com.ford.vinshare.domain.concessionaria.Concessionaria;
import br.com.ford.vinshare.domain.veiculo.Veiculo;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity(name = "Servico")
@Table(name = "servicos")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@EqualsAndHashCode(of = "id")
public class Servico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "veiculo_id", nullable = false)
    private Veiculo veiculo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "concessionaria_id", nullable = false)
    private Concessionaria concessionaria;

    @Column(name = "tipo_servico", nullable = false)
    private String tipoServico;

    @Column(name = "data_servico")
    private String dataServico;

    @Column(name = "valor_servico")
    private Double valorServico;

    @Column(name = "garantia_ativa")
    private Boolean garantiaAtiva;

    @Column(name = "status_servico")
    private String statusServico;
}