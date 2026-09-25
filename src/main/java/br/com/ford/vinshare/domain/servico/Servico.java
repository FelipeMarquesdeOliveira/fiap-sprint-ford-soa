package br.com.ford.vinshare.domain.servico;

import br.com.ford.vinshare.domain.cliente.Cliente;
import br.com.ford.vinshare.domain.concessionaria.Concessionaria;
import br.com.ford.vinshare.domain.veiculo.Veiculo;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity(name = "Servico")
@Table(name = "servicos")
@NoArgsConstructor
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

    /** Concessionária que executou o serviço. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "concessionaria_id", nullable = false)
    private Concessionaria concessionaria;

    @Column(name = "tipo_servico", nullable = false)
    private String tipoServico;

    @Column(name = "data_servico")
    private LocalDate dataServico;

    @Column(name = "valor_servico")
    private BigDecimal valorServico;

    @Column(name = "garantia_ativa")
    private Boolean garantiaAtiva;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "status_servico")
    private StatusServico statusServico;

    public Servico(DadosCadastroServico dados, Cliente cliente, Veiculo veiculo, Concessionaria concessionaria) {
        substituirDados(dados, cliente, veiculo, concessionaria);
    }

    /** PUT: substitui a representação completa do recurso. */
    public void substituirDados(DadosCadastroServico dados, Cliente cliente, Veiculo veiculo, Concessionaria concessionaria) {
        this.cliente = cliente;
        this.veiculo = veiculo;
        this.concessionaria = concessionaria;
        this.tipoServico = dados.tipoServico();
        this.dataServico = dados.dataServico();
        this.valorServico = dados.valorServico();
        this.garantiaAtiva = Boolean.TRUE.equals(dados.garantiaAtiva());
        this.statusServico = dados.statusServico();
    }

    /** PATCH: altera apenas os campos informados. */
    public void atualizarInformacoes(DadosAtualizacaoServico dados) {
        if (dados.tipoServico() != null) {
            this.tipoServico = dados.tipoServico();
        }
        if (dados.dataServico() != null) {
            this.dataServico = dados.dataServico();
        }
        if (dados.valorServico() != null) {
            this.valorServico = dados.valorServico();
        }
        if (dados.garantiaAtiva() != null) {
            this.garantiaAtiva = dados.garantiaAtiva();
        }
        if (dados.statusServico() != null) {
            this.statusServico = dados.statusServico();
        }
    }

    public Long getConcessionariaId() {
        return concessionaria.getId();
    }
}
