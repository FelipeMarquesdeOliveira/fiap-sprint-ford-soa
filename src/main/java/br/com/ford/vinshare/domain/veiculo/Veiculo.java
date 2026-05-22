package br.com.ford.vinshare.domain.veiculo;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity(name = "Veiculo")
@Table(name = "veiculos")
@NoArgsConstructor
@AllArgsConstructor
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
    private Double valorCompra;

    @Column(name = "tipo_veiculo")
    private String tipoVeiculo;
}