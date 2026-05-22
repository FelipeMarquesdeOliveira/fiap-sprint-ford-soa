package br.com.ford.vinshare.controller;

import br.com.ford.vinshare.domain.concessionaria.*;
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
@RequestMapping("/concessionarias")
@Tag(name = "Concessionárias", description = "Endpoints para gerenciamento de concessionárias Ford")
public class ConcessionariaController {

    @Autowired
    private ConcessionariaRepository repository;

    @PostMapping
    @Transactional
    @Operation(summary = "Cadastrar concessionária", description = "Cadastra uma nova concessionária na rede Ford")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Concessionária criada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos")
    })
    public ResponseEntity<DadosDetalhamentoConcessionaria> cadastrarConcessionaria(
            @RequestBody @Valid DadosCadastroConcessionaria dados,
            UriComponentsBuilder uriBuilder) {
        var concessionaria = dados.toEntity();
        repository.save(concessionaria);
        var uri = uriBuilder.path("/concessionarias/{id}").buildAndExpand(concessionaria.getId()).toUri();
        return ResponseEntity.created(uri).body(new DadosDetalhamentoConcessionaria(concessionaria));
    }

    @GetMapping
    @Operation(summary = "Listar concessionárias", description = "Lista todas as concessionárias ativas com paginação")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de concessionárias")
    })
    public ResponseEntity<Page<DadosListagemConcessionaria>> listarConcessionarias(Pageable paginacao) {
        var page = repository.findAllByAtivoTrue(paginacao).map(DadosListagemConcessionaria::new);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar concessionária por ID", description = "Retorna os detalhes de uma concessionária específica")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Concessionária encontrada"),
            @ApiResponse(responseCode = "404", description = "Concessionária não encontrada")
    })
    public ResponseEntity<DadosDetalhamentoConcessionaria> listarConcessionariaPorId(
            @Parameter(description = "ID da concessionária") @PathVariable Long id) {
        var concessionaria = repository.findById(id)
                .orElseThrow(() -> new ConcessionariaNotFoundException("Concessionária não encontrada"));
        return ResponseEntity.ok(new DadosDetalhamentoConcessionaria(concessionaria));
    }

    @PutMapping
    @Transactional
    @Operation(summary = "Atualizar concessionária", description = "Atualiza os dados de uma concessionária existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Concessionária atualizada"),
            @ApiResponse(responseCode = "404", description = "Concessionária não encontrada")
    })
    public ResponseEntity<DadosDetalhamentoConcessionaria> atualizarConcessionaria(
            @RequestBody @Valid DadosAtualizacaoConcessionaria dados) {
        var concessionaria = repository.findById(dados.id())
                .orElseThrow(() -> new ConcessionariaNotFoundException("Concessionária não encontrada"));
        dados.atualizarInformacoes(concessionaria);
        return ResponseEntity.ok(new DadosDetalhamentoConcessionaria(concessionaria));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional
    @Operation(summary = "Excluir concessionária", description = "Realiza soft delete de uma concessionária")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Concessionária excluída"),
            @ApiResponse(responseCode = "404", description = "Concessionária não encontrada")
    })
    public void excluirConcessionaria(
            @Parameter(description = "ID da concessionária") @PathVariable Long id) {
        var concessionaria = repository.findById(id)
                .orElseThrow(() -> new ConcessionariaNotFoundException("Concessionária não encontrada"));
        concessionaria.excluir();
    }
}