package br.com.ford.vinshare.controller;

import br.com.ford.vinshare.domain.autenticacao.DadosLogin;
import br.com.ford.vinshare.domain.autenticacao.DadosRefreshToken;
import br.com.ford.vinshare.domain.autenticacao.DadosTokenJWT;
import br.com.ford.vinshare.domain.usuario.Usuario;
import br.com.ford.vinshare.service.AutenticacaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticação")
public class AutenticacaoController {

    private final AutenticacaoService service;

    @PostMapping("/login")
    @SecurityRequirements
    @Operation(summary = "Login", description = "Autentica com e-mail e senha e retorna um access token JWT (15 min) e um refresh token (7 dias). Endpoint público.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Autenticado com sucesso"),
            @ApiResponse(responseCode = "401", description = "E-mail ou senha inválidos (codigo CREDENCIAIS_INVALIDAS)")
    })
    public ResponseEntity<DadosTokenJWT> login(@RequestBody @Valid DadosLogin dados) {
        return ResponseEntity.ok(service.login(dados));
    }

    @PostMapping("/refresh")
    @SecurityRequirements
    @Operation(summary = "Renovar tokens", description = "Troca um refresh token válido por um novo par de tokens. O refresh token usado é revogado (rotação); reutilizá-lo encerra todas as sessões do usuário. Endpoint público.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Novo par de tokens emitido"),
            @ApiResponse(responseCode = "401", description = "Refresh token inválido, expirado ou reutilizado")
    })
    public ResponseEntity<DadosTokenJWT> renovar(@RequestBody @Valid DadosRefreshToken dados) {
        return ResponseEntity.ok(service.renovar(dados.refreshToken()));
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout", description = "Revoga os refresh tokens do usuário e invalida imediatamente os access tokens já emitidos.")
    @ApiResponse(responseCode = "204", description = "Sessões encerradas")
    public ResponseEntity<Void> logout(@AuthenticationPrincipal Usuario usuario) {
        service.logout(usuario);
        return ResponseEntity.noContent().build();
    }
}
