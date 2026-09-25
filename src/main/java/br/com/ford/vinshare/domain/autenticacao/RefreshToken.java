package br.com.ford.vinshare.domain.autenticacao;

import br.com.ford.vinshare.domain.usuario.Usuario;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Refresh token opaco. Somente o hash SHA-256 é persistido; o valor original
 * é entregue uma única vez ao cliente no login/renovação.
 */
@Entity(name = "RefreshToken")
@Table(name = "refresh_tokens")
@NoArgsConstructor
@Getter
@EqualsAndHashCode(of = "id")
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "token_hash", nullable = false, unique = true)
    private String tokenHash;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "expira_em", nullable = false)
    private LocalDateTime expiraEm;

    private Boolean revogado = false;

    @Column(name = "criado_em", nullable = false)
    private LocalDateTime criadoEm;

    public RefreshToken(String tokenHash, Usuario usuario, LocalDateTime criadoEm, LocalDateTime expiraEm) {
        this.tokenHash = tokenHash;
        this.usuario = usuario;
        this.criadoEm = criadoEm;
        this.expiraEm = expiraEm;
        this.revogado = false;
    }

    public boolean isRevogado() {
        return Boolean.TRUE.equals(revogado);
    }

    public boolean isExpirado(LocalDateTime agora) {
        return !agora.isBefore(expiraEm);
    }

    public void revogar() {
        this.revogado = true;
    }
}
