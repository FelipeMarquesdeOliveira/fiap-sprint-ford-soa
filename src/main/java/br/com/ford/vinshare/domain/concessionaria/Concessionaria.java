package br.com.ford.vinshare.domain.concessionaria;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity(name = "Concessionaria")
@Table(name = "concessionarias")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@EqualsAndHashCode(of = "id")
public class Concessionaria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cnpj", nullable = false, unique = true)
    private String cnpj;

    @Column(name = "nome", nullable = false)
    private String nome;

    @Column(name = "regiao", nullable = false)
    private String regiao;

    @Column(name = "cidade")
    private String cidade;

    @Column(name = "estado")
    private String estado;

    @Column(name = "ativa")
    private Boolean ativa = true;
}