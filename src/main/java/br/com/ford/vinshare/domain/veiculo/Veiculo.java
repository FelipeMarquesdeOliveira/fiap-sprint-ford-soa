package br.com.ford.vinshare.domain.veiculo;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity(name = "Veiculo")
@Table(name = "veiculos")
@NoArgsConstructor
@Getter
@EqualsAndHashCode(of = "id")
public class Veiculo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "vin", nullable = false, unique = true)
    private String vin;

    @Column(name = "modelo", nullable = false)
    private String modelo;

    @Column(name = "versao", nullable = false)
    private String versao;

    @Column(name = "ano_fabricacao")
    private Integer anoFabricacao;

    @Column(name = "ano_modelo")
    private Integer anoModelo;

    @Column(name = "cor")
    private String cor;

    @Column(name = "combustivel")
    private String combustivel;

    @Column(name = "valor_compra")
    private BigDecimal valorCompra;

    @Column(name = "tipo_veiculo")
    private String tipoVeiculo;

    public Veiculo(DadosCadastroVeiculo dados) {
        substituirDados(dados);
    }

    /** PUT: substitui a representação completa do recurso. */
    public void substituirDados(DadosCadastroVeiculo dados) {
        this.vin = dados.vin();
        this.modelo = dados.modelo();
        this.versao = dados.versao();
        this.anoFabricacao = dados.anoFabricacao();
        this.anoModelo = dados.anoModelo();
        this.cor = dados.cor();
        this.combustivel = dados.combustivel();
        this.valorCompra = dados.valorCompra();
        this.tipoVeiculo = dados.tipoVeiculo();
    }

    /** PATCH: altera apenas os campos informados. O VIN é imutável. */
    public void atualizarInformacoes(DadosAtualizacaoVeiculo dados) {
        if (dados.modelo() != null) {
            this.modelo = dados.modelo();
        }
        if (dados.versao() != null) {
            this.versao = dados.versao();
        }
        if (dados.anoFabricacao() != null) {
            this.anoFabricacao = dados.anoFabricacao();
        }
        if (dados.anoModelo() != null) {
            this.anoModelo = dados.anoModelo();
        }
        if (dados.cor() != null) {
            this.cor = dados.cor();
        }
        if (dados.combustivel() != null) {
            this.combustivel = dados.combustivel();
        }
        if (dados.valorCompra() != null) {
            this.valorCompra = dados.valorCompra();
        }
        if (dados.tipoVeiculo() != null) {
            this.tipoVeiculo = dados.tipoVeiculo();
        }
    }
}
