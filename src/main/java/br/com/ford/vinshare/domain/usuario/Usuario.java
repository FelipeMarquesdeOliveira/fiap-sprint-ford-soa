package br.com.ford.vinshare.domain.usuario;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

@Entity(name = "Usuario")
@Table(name = "usuarios")
@NoArgsConstructor
@Getter
@EqualsAndHashCode(of = "id")
public class Usuario implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;

    private String email;

    private String senha;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    private Perfil perfil;

    /** Preenchido apenas para o perfil CONCESSIONARIA: delimita o escopo de dados do usuário. */
    @Column(name = "concessionaria_id")
    private Long concessionariaId;

    private Boolean ativo = true;

    /** Vai no claim "ver" do JWT; incrementar invalida todos os access tokens já emitidos. */
    @Column(name = "versao_token")
    private Integer versaoToken = 0;

    @Column(name = "criado_em")
    private LocalDateTime criadoEm;

    public Usuario(DadosCadastroUsuario dados, String senhaCriptografada) {
        this.nome = dados.nome();
        this.email = dados.email().toLowerCase();
        this.senha = senhaCriptografada;
        this.perfil = dados.perfil();
        this.concessionariaId = dados.perfil() == Perfil.CONCESSIONARIA ? dados.concessionariaId() : null;
        this.ativo = true;
        this.versaoToken = 0;
        this.criadoEm = LocalDateTime.now();
    }

    public void atualizarNome(String nome) {
        this.nome = nome;
    }

    public void alterarPerfil(Perfil perfil, Long concessionariaId) {
        this.perfil = perfil;
        this.concessionariaId = perfil == Perfil.CONCESSIONARIA ? concessionariaId : null;
        invalidarTokens();
    }

    public void ativar() {
        this.ativo = true;
    }

    public void desativar() {
        this.ativo = false;
        invalidarTokens();
    }

    public void invalidarTokens() {
        this.versaoToken = this.versaoToken + 1;
    }

    public boolean isConcessionaria() {
        return perfil == Perfil.CONCESSIONARIA;
    }

    /** ADMIN e ANALISTA enxergam toda a rede; CONCESSIONARIA apenas a própria concessionária. */
    public boolean podeAcessarConcessionaria(Long idConcessionaria) {
        return !isConcessionaria() || Objects.equals(this.concessionariaId, idConcessionaria);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + perfil.name()));
    }

    @Override
    public String getPassword() {
        return senha;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isEnabled() {
        return Boolean.TRUE.equals(ativo);
    }
}
