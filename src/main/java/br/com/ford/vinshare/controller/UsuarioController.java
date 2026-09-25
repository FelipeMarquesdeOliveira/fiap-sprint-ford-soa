package br.com.ford.vinshare.controller;

import br.com.ford.vinshare.domain.usuario.DadosAtualizacaoUsuario;
import br.com.ford.vinshare.domain.usuario.DadosCadastroUsuario;
import br.com.ford.vinshare.domain.usuario.DadosDetalhamentoUsuario;
import br.com.ford.vinshare.domain.usuario.Usuario;
import br.com.ford.vinshare.service.UsuarioService;
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
@RequestMapping("/usuarios")
@RequiredArgsConstructor
@Tag(name = "Usuários")
public class UsuarioController {

    private final UsuarioService service;

    @GetMapping("/me")
    @Operation(summary = "Usuário autenticado", description = "Retorna os dados do dono do token. Disponível para qualquer perfil.")
    public ResponseEntity<DadosDetalhamentoUsuario> meusDados(@AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(new DadosDetalhamentoUsuario(usuario));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Listar usuários", description = "Perfil: ADMIN.")
    public ResponseEntity<PagedModel<DadosDetalhamentoUsuario>> listar(
            @ParameterObject @PageableDefault(size = 10, sort = "nome") Pageable paginacao) {
        return ResponseEntity.ok(new PagedModel<>(service.listar(paginacao)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Detalhar usuário", description = "Perfil: ADMIN.")
    @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    public ResponseEntity<DadosDetalhamentoUsuario> detalhar(@Parameter(description = "ID do usuário") @PathVariable Long id) {
        return ResponseEntity.ok(service.detalhar(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cadastrar usuário", description = "Perfil: ADMIN. A senha é armazenada com hash BCrypt.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Usuário criado (header Location aponta para o recurso)"),
            @ApiResponse(responseCode = "409", description = "E-mail já cadastrado"),
            @ApiResponse(responseCode = "422", description = "Perfil CONCESSIONARIA sem concessionária válida")
    })
    public ResponseEntity<DadosDetalhamentoUsuario> cadastrar(@RequestBody @Valid DadosCadastroUsuario dados,
                                                              UriComponentsBuilder uriBuilder) {
        var usuario = service.cadastrar(dados);
        var uri = uriBuilder.path("/usuarios/{id}").buildAndExpand(usuario.id()).toUri();
        return ResponseEntity.created(uri).body(usuario);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Atualizar usuário parcialmente", description = "Perfil: ADMIN. Alterar perfil/concessionária ou desativar invalida na hora os tokens do usuário.")
    @ApiResponses({
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado"),
            @ApiResponse(responseCode = "422", description = "Regra violada (ex.: remover o próprio perfil ADMIN)")
    })
    public ResponseEntity<DadosDetalhamentoUsuario> atualizar(@PathVariable Long id,
                                                              @RequestBody @Valid DadosAtualizacaoUsuario dados,
                                                              @AuthenticationPrincipal Usuario logado) {
        return ResponseEntity.ok(service.atualizar(id, dados, logado));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Desativar usuário", description = "Perfil: ADMIN. Exclusão lógica; os tokens do usuário deixam de ser aceitos.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Usuário desativado"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado"),
            @ApiResponse(responseCode = "422", description = "Tentativa de desativar o próprio usuário")
    })
    public ResponseEntity<Void> desativar(@PathVariable Long id, @AuthenticationPrincipal Usuario logado) {
        service.desativar(id, logado);
        return ResponseEntity.noContent().build();
    }
}
