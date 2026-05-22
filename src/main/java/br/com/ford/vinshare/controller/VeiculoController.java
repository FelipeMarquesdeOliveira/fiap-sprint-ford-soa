package br.com.ford.vinshare.controller;

import br.com.ford.vinshare.domain.veiculo.*;
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
@RequestMapping("/veiculos")
@Tag(name = "Veículos", description = "Endpoints para gerenciamento de veículos Ford")
public class VeiculoController {

    @Autowired
    private VeiculoRepository repository;

    @PostMapping
    @Transactional
    @Operation(summary = "Cadastrar veículo", description = "Cadastra um novo veículo na base de dados")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Veículo criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos")
    })
    public ResponseEntity<DadosDetalhamentoVeiculo> cadastrarVeiculo(
            @RequestBody @Valid DadosCadastroVeiculo dados,
            UriComponentsBuilder uriBuilder) {
        var veiculo = dados.toEntity();
        repository.save(veiculo);
        var uri = uriBuilder.path("/veiculos/{id}").buildAndExpand(veiculo.getId()).toUri();
        return ResponseEntity.created(uri).body(new DadosDetalhamentoVeiculo(veiculo));
    }

    @GetMapping
    @Operation(summary = "Listar veículos", description = "Lista todos os veículos com paginação")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de veículos")
    })
    public ResponseEntity<Page<DadosListagemVeiculo>> listarVeiculos(Pageable paginacao) {
        var page = repository.findAll(paginacao).map(DadosListagemVeiculo::new);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar veículo por ID", description = "Retorna os detalhes de um veículo específico")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Veículo encontrado"),
            @ApiResponse(responseCode = "404", description = "Veículo não encontrado")
    })
    public ResponseEntity<DadosDetalhamentoVeiculo> listarVeiculoPorId(
            @Parameter(description = "ID do veículo") @PathVariable Long id) {
        var veiculo = repository.findById(id)
                .orElseThrow(() -> new VeiculoNotFoundException("Veículo não encontrado"));
        return ResponseEntity.ok(new DadosDetalhamentoVeiculo(veiculo));
    }

    @PutMapping
    @Transactional
    @Operation(summary = "Atualizar veículo", description = "Atualiza os dados de um veículo existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Veículo atualizado"),
            @ApiResponse(responseCode = "404", description = "Veículo não encontrado")
    })
    public ResponseEntity<DadosDetalhamentoVeiculo> atualizarVeiculo(
            @RequestBody @Valid DadosAtualizacaoVeiculo dados) {
        var veiculo = repository.findById(dados.id())
                .orElseThrow(() -> new VeiculoNotFoundException("Veículo não encontrado"));
        dados.atualizarInformacoes(veiculo);
        return ResponseEntity.ok(new DadosDetalhamentoVeiculo(veiculo));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional
    @Operation(summary = "Excluir veículo", description = "Remove um veículo da base de dados")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Veículo excluído"),
            @ApiResponse(responseCode = "404", description = "Veículo não encontrado")
    })
    public void excluirVeiculo(
            @Parameter(description = "ID do veículo") @PathVariable Long id) {
        repository.findById(id)
                .orElseThrow(() -> new VeiculoNotFoundException("Veículo não encontrado"));
        repository.deleteById(id);
    }
}