package br.com.ford.vinshare.controller;

import br.com.ford.vinshare.domain.cliente.*;
import br.com.ford.vinshare.domain.usuario.Usuario;
import br.com.ford.vinshare.service.ClienteService;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/clientes")
@RequiredArgsConstructor
@Tag(name = "Clientes")
public class ClienteController {

    private final ClienteService service;

    @GetMapping
    @Operation(summary = "Listar clientes", description = "Perfis: todos. CONCESSIONARIA recebe apenas os clientes da própria concessionária.")
    public ResponseEntity<PagedModel<DadosListagemCliente>> listar(
            @ParameterObject @PageableDefault(size = 10, sort = "nome") Pageable paginacao,
            @AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(new PagedModel<>(service.listar(usuario, paginacao)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalhar cliente", description = "Perfis: todos (CONCESSIONARIA apenas clientes próprios).")
    @ApiResponses({
            @ApiResponse(responseCode = "403", description = "Cliente de outra concessionária"),
            @ApiResponse(responseCode = "404", description = "Cliente não encontrado")
    })
    public ResponseEntity<DadosDetalhamentoCliente> detalhar(@Parameter(description = "ID do cliente") @PathVariable Long id,
                                                             @AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(service.detalhar(id, usuario));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CONCESSIONARIA')")
    @Operation(summary = "Cadastrar cliente", description = "Perfis: ADMIN, CONCESSIONARIA. Para CONCESSIONARIA a concessionária é obtida do token.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Cliente criado (header Location aponta para o recurso)"),
            @ApiResponse(responseCode = "403", description = "CONCESSIONARIA tentando cadastrar cliente de outra concessionária"),
            @ApiResponse(responseCode = "409", description = "CPF já cadastrado ou veículo já vinculado a outro cliente"),
            @ApiResponse(responseCode = "422", description = "Concessionária ou veículo inexistente")
    })
    public ResponseEntity<DadosDetalhamentoCliente> cadastrar(@RequestBody @Valid DadosCadastroCliente dados,
                                                              @AuthenticationPrincipal Usuario usuario,
                                                              UriComponentsBuilder uriBuilder) {
        var cliente = service.cadastrar(dados, usuario);
        var uri = uriBuilder.path("/clientes/{id}").buildAndExpand(cliente.id()).toUri();
        return ResponseEntity.created(uri).body(cliente);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONCESSIONARIA')")
    @Operation(summary = "Substituir cliente", description = "Perfis: ADMIN, CONCESSIONARIA (clientes próprios). PUT exige a representação completa.")
    @ApiResponses({
            @ApiResponse(responseCode = "403", description = "Cliente de outra concessionária"),
            @ApiResponse(responseCode = "404", description = "Cliente não encontrado"),
            @ApiResponse(responseCode = "409", description = "CPF pertence a outro cliente"),
            @ApiResponse(responseCode = "422", description = "Concessionária ou veículo inexistente")
    })
    public ResponseEntity<DadosDetalhamentoCliente> substituir(@PathVariable Long id,
                                                               @RequestBody @Valid DadosCadastroCliente dados,
                                                               @AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(service.substituir(id, dados, usuario));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONCESSIONARIA')")
    @Operation(summary = "Atualizar cliente parcialmente", description = "Perfis: ADMIN, CONCESSIONARIA (clientes próprios).")
    @ApiResponses({
            @ApiResponse(responseCode = "403", description = "Cliente de outra concessionária"),
            @ApiResponse(responseCode = "404", description = "Cliente não encontrado")
    })
    public ResponseEntity<DadosDetalhamentoCliente> atualizar(@PathVariable Long id,
                                                              @RequestBody @Valid DadosAtualizacaoCliente dados,
                                                              @AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(service.atualizar(id, dados, usuario));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONCESSIONARIA')")
    @Operation(summary = "Excluir cliente", description = "Perfis: ADMIN, CONCESSIONARIA (clientes próprios). Exclusão lógica.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Cliente excluído"),
            @ApiResponse(responseCode = "403", description = "Cliente de outra concessionária"),
            @ApiResponse(responseCode = "404", description = "Cliente não encontrado")
    })
    public ResponseEntity<Void> excluir(@PathVariable Long id, @AuthenticationPrincipal Usuario usuario) {
        service.excluir(id, usuario);
        return ResponseEntity.noContent().build();
    }
}
