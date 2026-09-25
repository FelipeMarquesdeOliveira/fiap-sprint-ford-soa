package br.com.ford.vinshare.domain.concessionaria;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity(name = "Concessionaria")
@Table(name = "concessionarias")
@NoArgsConstructor
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

    public Concessionaria(DadosCadastroConcessionaria dados) {
        substituirDados(dados);
        this.ativa = true;
    }

    /** PUT: substitui a representação completa do recurso. */
    public void substituirDados(DadosCadastroConcessionaria dados) {
        this.cnpj = dados.cnpj();
        this.nome = dados.nome();
        this.regiao = dados.regiao();
        this.cidade = dados.cidade();
        this.estado = dados.estado();
    }

    /** PATCH: altera apenas os campos informados. */
    public void atualizarInformacoes(DadosAtualizacaoConcessionaria dados) {
        if (dados.nome() != null) {
            this.nome = dados.nome();
        }
        if (dados.regiao() != null) {
            this.regiao = dados.regiao();
        }
        if (dados.cidade() != null) {
            this.cidade = dados.cidade();
        }
        if (dados.estado() != null) {
            this.estado = dados.estado();
        }
    }

    public void excluir() {
        this.ativa = false;
    }
}
