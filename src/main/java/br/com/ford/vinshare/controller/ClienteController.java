package br.com.ford.vinshare.controller;

import br.com.ford.vinshare.domain.cliente.*;
import br.com.ford.vinshare.domain.concessionaria.ConcessionariaRepository;
import br.com.ford.vinshare.domain.veiculo.VeiculoRepository;
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
@RequestMapping("/clientes")
@Tag(name = "Clientes", description = "Endpoints para gerenciamento de clientes das concessionárias")
public class ClienteController {

    @Autowired
    private ClienteRepository repository;

    @Autowired
    private VeiculoRepository veiculoRepository;

    @Autowired
    private ConcessionariaRepository concessionariaRepository;

    @PostMapping
    @Transactional
    @Operation(summary = "Cadastrar cliente", description = "Cadastra um novo cliente na base de dados")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Cliente criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos")
    })
    public ResponseEntity<DadosDetalhamentoCliente> cadastrarCliente(
            @RequestBody @Valid DadosCadastroCliente dados,
            UriComponentsBuilder uriBuilder) {
        var cliente = new Cliente();
        cliente.setCpf(dados.cpf());
        cliente.setNome(dados.nome());
        cliente.setEmail(dados.email());
        cliente.setTelefone(dados.telefone());
        cliente.setIdade(dados.idade());
        cliente.setSexo(dados.sexo());
        cliente.setRegiao(dados.regiao());
        cliente.setDataCompra(dados.dataCompra());
        cliente.setPerfilCliente(dados.perfilCliente());
        cliente.setAtivo(true);

        if (dados.veiculoId() != null) {
            veiculoRepository.findById(dados.veiculoId())
                    .ifPresent(cliente::setVeiculo);
        }
        if (dados.concessionariaId() != null) {
            concessionariaRepository.findById(dados.concessionariaId())
                    .ifPresent(cliente::setConcessionaria);
        }

        repository.save(cliente);
        var uri = uriBuilder.path("/clientes/{id}").buildAndExpand(cliente.getId()).toUri();
        return ResponseEntity.created(uri).body(new DadosDetalhamentoCliente(cliente));
    }

    @GetMapping
    @Operation(summary = "Listar clientes", description = "Lista todos os clientes ativos com paginação")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de clientes")
    })
    public ResponseEntity<Page<DadosListagemCliente>> listarClientes(Pageable paginacao) {
        var page = repository.findAllByAtivoTrue(paginacao).map(DadosListagemCliente::new);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar cliente por ID", description = "Retorna os detalhes de um cliente específico")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cliente encontrado"),
            @ApiResponse(responseCode = "404", description = "Cliente não encontrado")
    })
    public ResponseEntity<DadosDetalhamentoCliente> listarClientePorId(
            @Parameter(description = "ID do cliente") @PathVariable Long id) {
        var cliente = repository.findById(id)
                .orElseThrow(() -> new ClienteNotFoundException("Cliente não encontrado"));
        return ResponseEntity.ok(new DadosDetalhamentoCliente(cliente));
    }

    @PutMapping
    @Transactional
    @Operation(summary = "Atualizar cliente", description = "Atualiza os dados de um cliente existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cliente atualizado"),
            @ApiResponse(responseCode = "404", description = "Cliente não encontrado")
    })
    public ResponseEntity<DadosDetalhamentoCliente> atualizarCliente(
            @RequestBody @Valid DadosAtualizacaoCliente dados) {
        var cliente = repository.findById(dados.id())
                .orElseThrow(() -> new ClienteNotFoundException("Cliente não encontrado"));
        dados.atualizarInformacoes(cliente);
        return ResponseEntity.ok(new DadosDetalhamentoCliente(cliente));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional
    @Operation(summary = "Excluir cliente", description = "Realiza soft delete de um cliente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Cliente excluído"),
            @ApiResponse(responseCode = "404", description = "Cliente não encontrado")
    })
    public void excluirCliente(
            @Parameter(description = "ID do cliente") @PathVariable Long id) {
        var cliente = repository.findById(id)
                .orElseThrow(() -> new ClienteNotFoundException("Cliente não encontrado"));
        cliente.excluir();
    }
}