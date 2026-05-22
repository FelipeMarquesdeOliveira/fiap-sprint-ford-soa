package br.com.ford.vinshare.controller;

import br.com.ford.vinshare.domain.cliente.Cliente;
import br.com.ford.vinshare.domain.cliente.ClienteRepository;
import br.com.ford.vinshare.domain.concessionaria.ConcessionariaRepository;
import br.com.ford.vinshare.domain.servico.*;
import br.com.ford.vinshare.domain.veiculo.VeiculoRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/servicos")
public class ServicoController {

    @Autowired
    private ServicoRepository repository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private VeiculoRepository veiculoRepository;

    @Autowired
    private ConcessionariaRepository concessionariaRepository;

    @PostMapping
    @Transactional
    public ResponseEntity<DadosDetalhamentoServico> cadastrarServico(
            @RequestBody @Valid DadosCadastroServico dados,
            UriComponentsBuilder uriBuilder) {
        var cliente = clienteRepository.findById(dados.clienteId())
                .orElseThrow(() -> new ClienteNotFoundException("Cliente não encontrado"));
        var veiculo = veiculoRepository.findById(dados.veiculoId())
                .orElseThrow(() -> new VeiculoNotFoundException("Veículo não encontrado"));
        var concessionaria = concessionariaRepository.findById(dados.concessionariaId())
                .orElseThrow(() -> new ConcessionariaNotFoundException("Concessionária não encontrada"));

        var servico = new Servico();
        servico.setCliente(cliente);
        servico.setVeiculo(veiculo);
        servico.setConcessionaria(concessionaria);
        servico.setTipoServico(dados.tipoServico());
        servico.setDataServico(dados.dataServico());
        servico.setValorServico(dados.valorServico());
        servico.setGarantiaAtiva(dados.garantiaAtiva());
        servico.setStatusServico(dados.statusServico());

        repository.save(servico);
        var uri = uriBuilder.path("/servicos/{id}").buildAndExpand(servico.getId()).toUri();
        return ResponseEntity.created(uri).body(new DadosDetalhamentoServico(servico));
    }

    @GetMapping
    public ResponseEntity<Page<DadosListagemServico>> listarServicos(Pageable paginacao) {
        var page = repository.findAll(paginacao).map(DadosListagemServico::new);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DadosDetalhamentoServico> listarServicoPorId(@PathVariable Long id) {
        var servico = repository.findById(id)
                .orElseThrow(() -> new ServicoNotFoundException("Serviço não encontrado"));
        return ResponseEntity.ok(new DadosDetalhamentoServico(servico));
    }

    @PutMapping
    @Transactional
    public ResponseEntity<DadosDetalhamentoServico> atualizarServico(
            @RequestBody @Valid DadosAtualizacaoServico dados) {
        var servico = repository.findById(dados.id())
                .orElseThrow(() -> new ServicoNotFoundException("Serviço não encontrado"));
        dados.atualizarInformacoes(servico);
        return ResponseEntity.ok(new DadosDetalhamentoServico(servico));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional
    public void excluirServico(@PathVariable Long id) {
        repository.findById(id)
                .orElseThrow(() -> new ServicoNotFoundException("Serviço não encontrado"));
        repository.deleteById(id);
    }
}