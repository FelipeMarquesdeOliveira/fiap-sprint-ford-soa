package br.com.ford.vinshare.controller;

import br.com.ford.vinshare.domain.vinshare.ClienteRiscoResponse;
import br.com.ford.vinshare.domain.vinshare.DashboardResponse;
import br.com.ford.vinshare.domain.vinshare.VinShareResponse;
import br.com.ford.vinshare.domain.vinshare.VinShareService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/vinshare")
@Tag(name = "VIN Share", description = "Endpoints para análise de retenção e Service Share")
public class VinShareController {

    @Autowired
    private VinShareService vinShareService;

    @GetMapping("/dashboard")
    @Operation(summary = "Dashboard completo", description = "Retorna métricas agregadas de VIN Share geral, por concessionária, totais de clientes, veículos e serviços")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dashboard carregado com sucesso")
    })
    public ResponseEntity<DashboardResponse> getDashboard() {
        return ResponseEntity.ok(vinShareService.gerarDashboard());
    }

    @GetMapping("/concessionaria/{id}")
    @Operation(summary = "VIN Share por concessionária", description = "Calcula o percentual de VIN Share para uma concessionária específica")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "VIN Share calculado"),
            @ApiResponse(responseCode = "404", description = "Concessionária não encontrada")
    })
    public ResponseEntity<VinShareResponse> getVinSharePorConcessionaria(
            @Parameter(description = "ID da concessionária") @PathVariable Long id) {
        return ResponseEntity.ok(vinShareService.calcularVinSharePorConcessionaria(id));
    }

    @GetMapping("/concessionarias")
    @Operation(summary = "VIN Share por todas as concessionárias", description = "Lista o VIN Share de todas as concessionárias ativas")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de VIN Share por concessionária")
    })
    public ResponseEntity<List<VinShareResponse>> getVinShareGeral() {
        return ResponseEntity.ok(vinShareService.calcularVinShareGeral());
    }

    @GetMapping("/clientes-risco")
    @Operation(summary = "Clientes em risco", description = "Identifica clientes com alta probabilidade de sair da rede Ford (1 ou nenhum serviço)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de clientes em risco")
    })
    public ResponseEntity<List<ClienteRiscoResponse>> getClientesRisco(
            @Parameter(description = "ID da concessionária (opcional)") @RequestParam(required = false) Long concessionariaId) {
        return ResponseEntity.ok(vinShareService.identificarClientesRisco(concessionariaId));
    }
}