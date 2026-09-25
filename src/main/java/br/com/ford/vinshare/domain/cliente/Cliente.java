package br.com.ford.vinshare.domain.cliente;

import br.com.ford.vinshare.domain.concessionaria.Concessionaria;
import br.com.ford.vinshare.domain.veiculo.Veiculo;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;

@Entity(name = "Cliente")
@Table(name = "clientes")
@NoArgsConstructor
@Getter
@EqualsAndHashCode(of = "id")
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cpf", nullable = false, unique = true)
    private String cpf;

    @Column(name = "nome", nullable = false)
    private String nome;

    @Column(name = "email")
    private String email;

    @Column(name = "telefone")
    private String telefone;

    @Column(name = "idade")
    private Integer idade;

    @Column(name = "sexo")
    private String sexo;

    @Column(name = "regiao")
    private String regiao;

    @Column(name = "data_compra")
    private LocalDate dataCompra;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "veiculo_id")
    private Veiculo veiculo;

    /** Concessionária onde o veículo foi vendido (base do cálculo de VIN Share). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "concessionaria_id")
    private Concessionaria concessionaria;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "perfil_cliente")
    private PerfilCliente perfilCliente;

    @Column(name = "ativa")
    private Boolean ativo = true;

    public Cliente(DadosCadastroCliente dados, Veiculo veiculo, Concessionaria concessionaria) {
        substituirDados(dados, veiculo, concessionaria);
        this.ativo = true;
    }

    /** PUT: substitui a representação completa do recurso. */
    public void substituirDados(DadosCadastroCliente dados, Veiculo veiculo, Concessionaria concessionaria) {
        this.cpf = dados.cpf();
        this.nome = dados.nome();
        this.email = dados.email();
        this.telefone = dados.telefone();
        this.idade = dados.idade();
        this.sexo = dados.sexo();
        this.regiao = dados.regiao();
        this.dataCompra = dados.dataCompra();
        this.perfilCliente = dados.perfilCliente();
        this.veiculo = veiculo;
        this.concessionaria = concessionaria;
    }

    /** PATCH: altera apenas os campos informados. CPF, veículo e concessionária são imutáveis aqui. */
    public void atualizarInformacoes(DadosAtualizacaoCliente dados) {
        if (dados.nome() != null) {
            this.nome = dados.nome();
        }
        if (dados.email() != null) {
            this.email = dados.email();
        }
        if (dados.telefone() != null) {
            this.telefone = dados.telefone();
        }
        if (dados.idade() != null) {
            this.idade = dados.idade();
        }
        if (dados.sexo() != null) {
            this.sexo = dados.sexo();
        }
        if (dados.regiao() != null) {
            this.regiao = dados.regiao();
        }
        if (dados.perfilCliente() != null) {
            this.perfilCliente = dados.perfilCliente();
        }
    }

    public Long getConcessionariaId() {
        return concessionaria != null ? concessionaria.getId() : null;
    }

    public void excluir() {
        this.ativo = false;
    }
}
