package br.com.ford.vinshare.domain;

import br.com.ford.vinshare.domain.autenticacao.RefreshToken;
import br.com.ford.vinshare.domain.servico.StatusServico;
import br.com.ford.vinshare.domain.usuario.DadosCadastroUsuario;
import br.com.ford.vinshare.domain.usuario.Perfil;
import br.com.ford.vinshare.domain.usuario.Usuario;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Regras de domínio")
class RegrasDeDominioTest {

    @Test
    @DisplayName("CONCESSIONARIA só acessa a própria concessionária; ADMIN e ANALISTA acessam todas")
    void escopoPorConcessionaria() {
        var concessionaria = usuario(Perfil.CONCESSIONARIA, 1L);
        var analista = usuario(Perfil.ANALISTA, null);

        assertThat(concessionaria.podeAcessarConcessionaria(1L)).isTrue();
        assertThat(concessionaria.podeAcessarConcessionaria(2L)).isFalse();
        assertThat(analista.podeAcessarConcessionaria(2L)).isTrue();
    }

    @Test
    @DisplayName("perfis diferentes de CONCESSIONARIA não ficam vinculados a uma concessionária")
    void vinculoApenasParaConcessionaria() {
        assertThat(usuario(Perfil.ADMIN, 1L).getConcessionariaId()).isNull();
        assertThat(usuario(Perfil.CONCESSIONARIA, 1L).getConcessionariaId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("authority do Spring Security segue o padrão ROLE_<PERFIL>")
    void authorityPorPerfil() {
        assertThat(usuario(Perfil.ANALISTA, null).getAuthorities())
                .extracting(Object::toString).containsExactly("ROLE_ANALISTA");
    }

    @Test
    @DisplayName("trocar perfil ou desativar incrementa a versão do token (revoga tokens emitidos)")
    void revogacaoPorVersao() {
        var usuario = usuario(Perfil.ANALISTA, null);

        usuario.alterarPerfil(Perfil.ADMIN, null);
        assertThat(usuario.getVersaoToken()).isEqualTo(1);

        usuario.desativar();
        assertThat(usuario.getVersaoToken()).isEqualTo(2);
        assertThat(usuario.isEnabled()).isFalse();
    }

    @Test
    @DisplayName("CONCLUIDO e CANCELADO são status finais")
    void statusFinais() {
        assertThat(StatusServico.CONCLUIDO.isFinal()).isTrue();
        assertThat(StatusServico.CANCELADO.isFinal()).isTrue();
        assertThat(StatusServico.AGENDADO.isFinal()).isFalse();
        assertThat(StatusServico.EM_ANDAMENTO.isFinal()).isFalse();
    }

    @Test
    @DisplayName("refresh token expira no instante configurado e pode ser revogado")
    void refreshTokenExpiraERevoga() {
        var agora = LocalDateTime.of(2026, 9, 25, 10, 0);
        var token = new RefreshToken("hash", usuario(Perfil.ADMIN, null), agora, agora.plusDays(7));

        assertThat(token.isExpirado(agora.plusDays(6))).isFalse();
        assertThat(token.isExpirado(agora.plusDays(7))).isTrue();

        token.revogar();
        assertThat(token.isRevogado()).isTrue();
    }

    private static Usuario usuario(Perfil perfil, Long concessionariaId) {
        return new Usuario(new DadosCadastroUsuario("Teste", "Teste@VinShare.test", "Senha@123", perfil, concessionariaId), "hash");
    }
}
