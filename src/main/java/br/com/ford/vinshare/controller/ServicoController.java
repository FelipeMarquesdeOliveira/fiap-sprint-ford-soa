package br.com.ford.vinshare.controller;

import br.com.ford.vinshare.domain.cliente.ClienteNotFoundException;
import br.com.ford.vinshare.domain.concessionaria.ConcessionariaNotFoundException;
import br.com.ford.vinshare.domain.servico.*;
import br.com.ford.vinshare.domain.veiculo.VeiculoNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/servicos")
@Tag(name = "Serviços", description = "Endpoints para gerenciamento de serviços de manutenção")
public class ServicoController {

    @Autowired
    private ServicoRepository repository;

    @Autowired
    private br.com.ford.vinshare.domain.cliente.ClienteRepository clienteRepository;

    @Autowired
    private br.com.ford.vinshare.domain.veiculo.VeiculoRepository veiculoRepository;

    @Autowired
    private br.com.ford.vinshare.domain.concessionaria.ConcessionariaRepository concessionariaRepository;

    @PostMapping
    @Transactional
    @Operation(summary = "Cadastrar serviço", description = "Registra um novo serviço de manutenção")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Serviço criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Cliente, veículo ou concessionária não encontrado")
    })
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
    @Operation(summary = "Listar serviços", description = "Lista todos os serviços com paginação")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de serviços")
    })
    public ResponseEntity<Page<DadosListagemServico>> listarServicos(Pageable paginacao) {
        var page = repository.findAll(paginacao).map(DadosListagemServico::new);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar serviço por ID", description = "Retorna os detalhes de um serviço específico")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Serviço encontrado"),
            @ApiResponse(responseCode = "404", description = "Serviço não encontrado")
    })
    public ResponseEntity<DadosDetalhamentoServico> listarServicoPorId(
            @Parameter(description = "ID do serviço") @PathVariable Long id) {
        var servico = repository.findById(id)
                .orElseThrow(() -> new ServicoNotFoundException("Serviço não encontrado"));
        return ResponseEntity.ok(new DadosDetalhamentoServico(servico));
    }

    @PutMapping
    @Transactional
    @Operation(summary = "Atualizar serviço", description = "Atualiza os dados de um serviço existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Serviço atualizado"),
            @ApiResponse(responseCode = "404", description = "Serviço não encontrado")
    })
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
    @Operation(summary = "Excluir serviço", description = "Remove um serviço da base de dados")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Serviço excluído"),
            @ApiResponse(responseCode = "404", description = "Serviço não encontrado")
    })
    public void excluirServico(
            @Parameter(description = "ID do serviço") @PathVariable Long id) {
        repository.findById(id)
                .orElseThrow(() -> new ServicoNotFoundException("Serviço não encontrado"));
        repository.deleteById(id);
    }
}