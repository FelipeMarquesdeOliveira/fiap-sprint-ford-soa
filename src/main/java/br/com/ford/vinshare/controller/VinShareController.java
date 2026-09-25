package br.com.ford.vinshare.controller;

import br.com.ford.vinshare.domain.usuario.Usuario;
import br.com.ford.vinshare.domain.vinshare.ClienteRiscoResponse;
import br.com.ford.vinshare.domain.vinshare.DashboardResponse;
import br.com.ford.vinshare.domain.vinshare.VinShareResponse;
import br.com.ford.vinshare.service.VinShareService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/vinshare")
@RequiredArgsConstructor
@Tag(name = "VIN Share")
public class VinShareController {

    private final VinShareService service;

    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALISTA')")
    @Operation(summary = "Dashboard da rede", description = "Perfis: ADMIN, ANALISTA. VIN Share geral e por concessionária, totais e clientes em risco.")
    public ResponseEntity<DashboardResponse> dashboard() {
        return ResponseEntity.ok(service.gerarDashboard());
    }

    @GetMapping("/concessionarias")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALISTA')")
    @Operation(summary = "VIN Share de todas as concessionárias", description = "Perfis: ADMIN, ANALISTA.")
    public ResponseEntity<List<VinShareResponse>> porConcessionaria() {
        return ResponseEntity.ok(service.calcularPorConcessionaria());
    }

    @GetMapping("/concessionarias/{id}")
    @Operation(summary = "VIN Share de uma concessionária", description = "Perfis: ADMIN, ANALISTA; CONCESSIONARIA somente a própria.")
    @ApiResponses({
            @ApiResponse(responseCode = "403", description = "CONCESSIONARIA consultando outra concessionária"),
            @ApiResponse(responseCode = "404", description = "Concessionária não encontrada")
    })
    public ResponseEntity<VinShareResponse> daConcessionaria(@Parameter(description = "ID da concessionária") @PathVariable Long id,
                                                             @AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(service.calcularDaConcessionaria(id, usuario));
    }

    @GetMapping("/clientes-risco")
    @Operation(summary = "Clientes em risco de evasão", description = "Perfis: todos. Clientes com 0 ou 1 serviço concluído na rede; CONCESSIONARIA recebe apenas os próprios. CPF mascarado.")
    @ApiResponse(responseCode = "403", description = "CONCESSIONARIA filtrando por outra concessionária")
    public ResponseEntity<List<ClienteRiscoResponse>> clientesRisco(
            @Parameter(description = "Filtra por concessionária (opcional)") @RequestParam(required = false) Long concessionariaId,
            @AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(service.identificarClientesRisco(concessionariaId, usuario));
    }
}
