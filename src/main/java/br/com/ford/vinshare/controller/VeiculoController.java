package br.com.ford.vinshare.controller;

import br.com.ford.vinshare.domain.servico.DadosListagemServico;
import br.com.ford.vinshare.domain.veiculo.*;
import br.com.ford.vinshare.service.VeiculoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@RestController
@RequestMapping("/veiculos")
@RequiredArgsConstructor
@Tag(name = "Veículos")
public class VeiculoController {

    private final VeiculoService service;

    @GetMapping
    @Operation(summary = "Listar veículos", description = "Perfis: todos. Filtro opcional por modelo.")
    public ResponseEntity<PagedModel<DadosListagemVeiculo>> listar(
            @Parameter(description = "Parte do nome do modelo (ex.: ranger)") @RequestParam(required = false) String modelo,
            @ParameterObject @PageableDefault(size = 10, sort = "modelo") Pageable paginacao) {
        return ResponseEntity.ok(new PagedModel<>(service.listar(modelo, paginacao)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalhar veículo", description = "Perfis: todos.")
    @ApiResponse(responseCode = "404", description = "Veículo não encontrado")
    public ResponseEntity<DadosDetalhamentoVeiculo> detalhar(@Parameter(description = "ID do veículo") @PathVariable Long id) {
        return ResponseEntity.ok(service.detalhar(id));
    }

    @GetMapping("/{id}/servicos")
    @Operation(summary = "Histórico de serviços do veículo", description = "Perfis: todos. Sub-recurso com os serviços do VIN em toda a rede oficial.")
    @ApiResponse(responseCode = "404", description = "Veículo não encontrado")
    public ResponseEntity<List<DadosListagemServico>> historico(@PathVariable Long id) {
        return ResponseEntity.ok(service.historicoDeServicos(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CONCESSIONARIA')")
    @Operation(summary = "Cadastrar veículo", description = "Perfis: ADMIN, CONCESSIONARIA.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Veículo criado (header Location aponta para o recurso)"),
            @ApiResponse(responseCode = "409", description = "VIN já cadastrado")
    })
    public ResponseEntity<DadosDetalhamentoVeiculo> cadastrar(@RequestBody @Valid DadosCadastroVeiculo dados,
                                                              UriComponentsBuilder uriBuilder) {
        var veiculo = service.cadastrar(dados);
        var uri = uriBuilder.path("/veiculos/{id}").buildAndExpand(veiculo.id()).toUri();
        return ResponseEntity.created(uri).body(veiculo);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONCESSIONARIA')")
    @Operation(summary = "Substituir veículo", description = "Perfis: ADMIN, CONCESSIONARIA. PUT exige a representação completa.")
    @ApiResponses({
            @ApiResponse(responseCode = "404", description = "Veículo não encontrado"),
            @ApiResponse(responseCode = "409", description = "VIN pertence a outro veículo")
    })
    public ResponseEntity<DadosDetalhamentoVeiculo> substituir(@PathVariable Long id,
                                                               @RequestBody @Valid DadosCadastroVeiculo dados) {
        return ResponseEntity.ok(service.substituir(id, dados));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONCESSIONARIA')")
    @Operation(summary = "Atualizar veículo parcialmente", description = "Perfis: ADMIN, CONCESSIONARIA. O VIN é imutável.")
    @ApiResponse(responseCode = "404", description = "Veículo não encontrado")
    public ResponseEntity<DadosDetalhamentoVeiculo> atualizar(@PathVariable Long id,
                                                              @RequestBody @Valid DadosAtualizacaoVeiculo dados) {
        return ResponseEntity.ok(service.atualizar(id, dados));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Excluir veículo", description = "Perfil: ADMIN. Só é permitido para veículos sem cliente ou serviços vinculados.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Veículo excluído"),
            @ApiResponse(responseCode = "404", description = "Veículo não encontrado"),
            @ApiResponse(responseCode = "409", description = "Veículo vinculado a cliente ou serviços")
    })
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
