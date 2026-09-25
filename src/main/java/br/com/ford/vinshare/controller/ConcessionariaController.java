package br.com.ford.vinshare.controller;

import br.com.ford.vinshare.domain.concessionaria.*;
import br.com.ford.vinshare.service.ConcessionariaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
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

@RestController
@RequestMapping("/concessionarias")
@RequiredArgsConstructor
@Tag(name = "Concessionárias")
public class ConcessionariaController {

    private final ConcessionariaService service;

    @GetMapping
    @SecurityRequirements
    @Operation(summary = "Listar concessionárias", description = "Endpoint público: localização da rede oficial. Filtro opcional por UF.")
    public ResponseEntity<PagedModel<DadosListagemConcessionaria>> listar(
            @Parameter(description = "Sigla da UF (ex.: SP)") @RequestParam(required = false) String estado,
            @ParameterObject @PageableDefault(size = 10, sort = "nome") Pageable paginacao) {
        return ResponseEntity.ok(new PagedModel<>(service.listar(estado, paginacao)));
    }

    @GetMapping("/{id}")
    @SecurityRequirements
    @Operation(summary = "Detalhar concessionária", description = "Endpoint público.")
    @ApiResponse(responseCode = "404", description = "Concessionária não encontrada")
    public ResponseEntity<DadosDetalhamentoConcessionaria> detalhar(
            @Parameter(description = "ID da concessionária") @PathVariable Long id) {
        return ResponseEntity.ok(service.detalhar(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cadastrar concessionária", description = "Perfil: ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Concessionária criada (header Location aponta para o recurso)"),
            @ApiResponse(responseCode = "409", description = "CNPJ já cadastrado")
    })
    public ResponseEntity<DadosDetalhamentoConcessionaria> cadastrar(@RequestBody @Valid DadosCadastroConcessionaria dados,
                                                                     UriComponentsBuilder uriBuilder) {
        var concessionaria = service.cadastrar(dados);
        var uri = uriBuilder.path("/concessionarias/{id}").buildAndExpand(concessionaria.id()).toUri();
        return ResponseEntity.created(uri).body(concessionaria);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Substituir concessionária", description = "Perfil: ADMIN. PUT exige a representação completa do recurso.")
    @ApiResponses({
            @ApiResponse(responseCode = "404", description = "Concessionária não encontrada"),
            @ApiResponse(responseCode = "409", description = "CNPJ pertence a outra concessionária")
    })
    public ResponseEntity<DadosDetalhamentoConcessionaria> substituir(@PathVariable Long id,
                                                                      @RequestBody @Valid DadosCadastroConcessionaria dados) {
        return ResponseEntity.ok(service.substituir(id, dados));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Atualizar concessionária parcialmente", description = "Perfil: ADMIN. Apenas os campos enviados são alterados.")
    @ApiResponse(responseCode = "404", description = "Concessionária não encontrada")
    public ResponseEntity<DadosDetalhamentoConcessionaria> atualizar(@PathVariable Long id,
                                                                     @RequestBody @Valid DadosAtualizacaoConcessionaria dados) {
        return ResponseEntity.ok(service.atualizar(id, dados));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Excluir concessionária", description = "Perfil: ADMIN. Exclusão lógica (o histórico é preservado).")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Concessionária excluída"),
            @ApiResponse(responseCode = "404", description = "Concessionária não encontrada")
    })
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
