package br.com.ford.vinshare.controller;

import br.com.ford.vinshare.domain.vinshare.ClienteRiscoResponse;
import br.com.ford.vinshare.domain.vinshare.DashboardResponse;
import br.com.ford.vinshare.domain.vinshare.VinShareResponse;
import br.com.ford.vinshare.domain.vinshare.VinShareService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/vinshare")
public class VinShareController {

    @Autowired
    private VinShareService vinShareService;

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardResponse> getDashboard() {
        return ResponseEntity.ok(vinShareService.gerarDashboard());
    }

    @GetMapping("/concessionaria/{id}")
    public ResponseEntity<VinShareResponse> getVinSharePorConcessionaria(@PathVariable Long id) {
        return ResponseEntity.ok(vinShareService.calcularVinSharePorConcessionaria(id));
    }

    @GetMapping("/concessionarias")
    public ResponseEntity<List<VinShareResponse>> getVinShareGeral() {
        return ResponseEntity.ok(vinShareService.calcularVinShareGeral());
    }

    @GetMapping("/clientes-risco")
    public ResponseEntity<List<ClienteRiscoResponse>> getClientesRisco(
            @RequestParam(required = false) Long concessionariaId) {
        return ResponseEntity.ok(vinShareService.identificarClientesRisco(concessionariaId));
    }
}