package br.com.ford.vinshare.service;

import br.com.ford.vinshare.domain.cliente.Cliente;
import br.com.ford.vinshare.domain.cliente.ClienteRepository;
import br.com.ford.vinshare.domain.cliente.DadosCadastroCliente;
import br.com.ford.vinshare.domain.cliente.PerfilCliente;
import br.com.ford.vinshare.domain.concessionaria.Concessionaria;
import br.com.ford.vinshare.domain.concessionaria.ConcessionariaRepository;
import br.com.ford.vinshare.domain.concessionaria.DadosCadastroConcessionaria;
import br.com.ford.vinshare.domain.exception.AcessoNegadoException;
import br.com.ford.vinshare.domain.servico.ServicoRepository;
import br.com.ford.vinshare.domain.servico.StatusServico;
import br.com.ford.vinshare.domain.usuario.DadosCadastroUsuario;
import br.com.ford.vinshare.domain.usuario.Perfil;
import br.com.ford.vinshare.domain.usuario.Usuario;
import br.com.ford.vinshare.domain.veiculo.DadosCadastroVeiculo;
import br.com.ford.vinshare.domain.veiculo.Veiculo;
import br.com.ford.vinshare.domain.vinshare.ClienteRiscoResponse.NivelRisco;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("VinShareService - cálculo de VIN Share e clientes em risco")
class VinShareServiceTest {

    @Mock
    private ConcessionariaRepository concessionariaRepository;
    @Mock
    private ClienteRepository clienteRepository;
    @Mock
    private ServicoRepository servicoRepository;
    @InjectMocks
    private VinShareService service;

    private Concessionaria norte;
    private Concessionaria sul;

    @BeforeEach
    void configurar() {
        norte = concessionaria(1L, "Ford Norte");
        sul = concessionaria(2L, "Ford Sul");

        // Norte vendeu 3 veículos: cliente 10 (3 serviços), 11 (1 serviço), 12 (nenhum). Sul vendeu 1: cliente 20 (2 serviços).
        var clientes = List.of(cliente(10L, norte), cliente(11L, norte), cliente(12L, norte), cliente(20L, sul));
        lenient().when(clienteRepository.findAllByAtivoTrueAndVeiculoIsNotNullAndConcessionariaIsNotNull()).thenReturn(clientes);
        lenient().when(servicoRepository.contarPorClienteComStatus(StatusServico.CONCLUIDO)).thenReturn(List.of(
                new Object[]{10L, 3L}, new Object[]{11L, 1L}, new Object[]{20L, 2L}));
        lenient().when(servicoRepository.somarReceitaPorConcessionariaComStatus(StatusServico.CONCLUIDO)).thenReturn(List.of(
                new Object[]{1L, new BigDecimal("3000.5")}, new Object[]{2L, new BigDecimal("800")}));
        lenient().when(concessionariaRepository.findAllByAtivaTrueOrderByNome()).thenReturn(List.of(norte, sul));
    }

    @Test
    @DisplayName("VIN Share = veículos com serviço concluído / veículos vendidos, com 2 casas decimais")
    void calculaVinSharePorConcessionaria() {
        var resultado = service.calcularPorConcessionaria();

        var vinShareNorte = resultado.getFirst();
        assertThat(vinShareNorte.totalVeiculos()).isEqualTo(3);
        assertThat(vinShareNorte.veiculosComServico()).isEqualTo(2);
        assertThat(vinShareNorte.veiculosSemServico()).isEqualTo(1);
        assertThat(vinShareNorte.vinSharePercentual()).isEqualTo(66.67);
        assertThat(vinShareNorte.receitaTotalServicos()).isEqualByComparingTo("3000.50");
        assertThat(resultado.get(1).vinSharePercentual()).isEqualTo(100.0);
    }

    @Test
    @DisplayName("dashboard consolida a rede inteira")
    void dashboardConsolidaRede() {
        when(clienteRepository.countByAtivoTrue()).thenReturn(4L);
        when(servicoRepository.countByStatusServico(StatusServico.CONCLUIDO)).thenReturn(6L);

        var dashboard = service.gerarDashboard();

        assertThat(dashboard.vinShareGeral().totalVeiculos()).isEqualTo(4);
        assertThat(dashboard.vinShareGeral().veiculosComServico()).isEqualTo(3);
        assertThat(dashboard.vinShareGeral().vinSharePercentual()).isEqualTo(75.0);
        assertThat(dashboard.receitaTotal()).isEqualByComparingTo("3800.50");
        assertThat(dashboard.clientesEmRisco()).isEqualTo(2);
        assertThat(dashboard.totalServicosConcluidos()).isEqualTo(6);
    }

    @Test
    @DisplayName("cliente sem serviço é risco ALTO; com um serviço é risco MEDIO; com dois ou mais não está em risco")
    void classificaClientesEmRisco() {
        var riscos = service.identificarClientesRisco(null, usuario(Perfil.ANALISTA, null));

        assertThat(riscos).extracting(r -> r.id()).containsExactly(12L, 11L);
        assertThat(riscos).extracting(r -> r.nivelRisco()).containsExactly(NivelRisco.ALTO, NivelRisco.MEDIO);
        assertThat(riscos.getFirst().cpfMascarado()).isEqualTo("529.***.***-25");
    }

    @Test
    @DisplayName("usuário CONCESSIONARIA recebe apenas os clientes em risco da própria concessionária")
    void clientesEmRiscoRespeitamEscopo() {
        var riscos = service.identificarClientesRisco(null, usuario(Perfil.CONCESSIONARIA, 2L));

        assertThat(riscos).isEmpty();
        assertThatThrownBy(() -> service.identificarClientesRisco(1L, usuario(Perfil.CONCESSIONARIA, 2L)))
                .isInstanceOf(AcessoNegadoException.class);
    }

    @Test
    @DisplayName("concessionária sem vendas tem VIN Share 0 (sem divisão por zero)")
    void semVendasNaoDivideporZero() {
        var vazia = concessionaria(3L, "Ford Nova");
        when(concessionariaRepository.findByIdAndAtivaTrue(3L)).thenReturn(Optional.of(vazia));

        var resultado = service.calcularDaConcessionaria(3L, usuario(Perfil.ADMIN, null));

        assertThat(resultado.totalVeiculos()).isZero();
        assertThat(resultado.vinSharePercentual()).isZero();
        assertThat(resultado.receitaTotalServicos()).isEqualByComparingTo("0.00");
    }

    private static Concessionaria concessionaria(Long id, String nome) {
        var concessionaria = new Concessionaria(new DadosCadastroConcessionaria("11.222.333/0001-81", nome, "Sudeste", "Cidade", "SP"));
        ReflectionTestUtils.setField(concessionaria, "id", id);
        return concessionaria;
    }

    private static Cliente cliente(Long id, Concessionaria concessionaria) {
        var veiculo = new Veiculo(new DadosCadastroVeiculo("9BFZH55L8R800" + String.format("%04d", id), "Ranger", "XLS",
                2024, 2024, "Preto", "Diesel", BigDecimal.TEN, "Picape"));
        var cliente = new Cliente(new DadosCadastroCliente("529.982.247-25", "Cliente " + id, null, null, 30, "F", "Sudeste",
                null, null, null, PerfilCliente.FIEL), veiculo, concessionaria);
        ReflectionTestUtils.setField(cliente, "id", id);
        return cliente;
    }

    private static Usuario usuario(Perfil perfil, Long concessionariaId) {
        return new Usuario(new DadosCadastroUsuario("Teste", "teste@vinshare.test", "Senha@123", perfil, concessionariaId), "hash");
    }
}
