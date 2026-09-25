package br.com.ford.vinshare.controller;

import br.com.ford.vinshare.domain.servico.*;
import br.com.ford.vinshare.domain.usuario.Usuario;
import br.com.ford.vinshare.service.ServicoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/servicos")
@RequiredArgsConstructor
@Tag(name = "Serviços")
public class ServicoController {

    private final ServicoService service;

    @GetMapping
    @Operation(summary = "Listar serviços", description = "Perfis: todos. CONCESSIONARIA recebe apenas os serviços que executou. Filtro opcional por status.")
    public ResponseEntity<PagedModel<DadosListagemServico>> listar(
            @Parameter(description = "Status do serviço") @RequestParam(required = false) StatusServico status,
            @ParameterObject @PageableDefault(size = 10, sort = "dataServico", direction = Sort.Direction.DESC) Pageable paginacao,
            @AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(new PagedModel<>(service.listar(status, usuario, paginacao)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalhar serviço", description = "Perfis: todos (CONCESSIONARIA apenas serviços próprios).")
    @ApiResponses({
            @ApiResponse(responseCode = "403", description = "Serviço de outra concessionária"),
            @ApiResponse(responseCode = "404", description = "Serviço não encontrado")
    })
    public ResponseEntity<DadosDetalhamentoServico> detalhar(@Parameter(description = "ID do serviço") @PathVariable Long id,
                                                             @AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(service.detalhar(id, usuario));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CONCESSIONARIA')")
    @Operation(summary = "Registrar serviço", description = "Perfis: ADMIN, CONCESSIONARIA. O veículo deve pertencer ao cliente e um serviço CONCLUIDO não pode ter data futura.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Serviço registrado (header Location aponta para o recurso)"),
            @ApiResponse(responseCode = "403", description = "CONCESSIONARIA tentando registrar serviço de outra concessionária"),
            @ApiResponse(responseCode = "422", description = "Regra de negócio violada (cliente/veículo inválido, data futura...)")
    })
    public ResponseEntity<DadosDetalhamentoServico> cadastrar(@RequestBody @Valid DadosCadastroServico dados,
                                                              @AuthenticationPrincipal Usuario usuario,
                                                              UriComponentsBuilder uriBuilder) {
        var servico = service.cadastrar(dados, usuario);
        var uri = uriBuilder.path("/servicos/{id}").buildAndExpand(servico.id()).toUri();
        return ResponseEntity.created(uri).body(servico);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONCESSIONARIA')")
    @Operation(summary = "Substituir serviço", description = "Perfis: ADMIN, CONCESSIONARIA (serviços próprios). Serviços CONCLUIDO/CANCELADO são imutáveis.")
    @ApiResponses({
            @ApiResponse(responseCode = "403", description = "Serviço de outra concessionária"),
            @ApiResponse(responseCode = "404", description = "Serviço não encontrado"),
            @ApiResponse(responseCode = "422", description = "Serviço finalizado ou regra de negócio violada")
    })
    public ResponseEntity<DadosDetalhamentoServico> substituir(@PathVariable Long id,
                                                               @RequestBody @Valid DadosCadastroServico dados,
                                                               @AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(service.substituir(id, dados, usuario));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONCESSIONARIA')")
    @Operation(summary = "Atualizar serviço parcialmente", description = "Perfis: ADMIN, CONCESSIONARIA (serviços próprios). Ex.: {\"statusServico\": \"CONCLUIDO\"}.")
    @ApiResponses({
            @ApiResponse(responseCode = "403", description = "Serviço de outra concessionária"),
            @ApiResponse(responseCode = "404", description = "Serviço não encontrado"),
            @ApiResponse(responseCode = "422", description = "Serviço finalizado ou regra de negócio violada")
    })
    public ResponseEntity<DadosDetalhamentoServico> atualizar(@PathVariable Long id,
                                                              @RequestBody @Valid DadosAtualizacaoServico dados,
                                                              @AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(service.atualizar(id, dados, usuario));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Excluir serviço", description = "Perfil: ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Serviço excluído"),
            @ApiResponse(responseCode = "404", description = "Serviço não encontrado")
    })
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
