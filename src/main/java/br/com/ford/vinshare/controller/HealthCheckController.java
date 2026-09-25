package br.com.ford.vinshare.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping("/health-check")
@Tag(name = "Health Check")
public class HealthCheckController {

    @GetMapping
    @SecurityRequirements
    @Operation(summary = "Health check", description = "Endpoint público para monitoramento da disponibilidade da API.")
    public ResponseEntity<DadosHealthCheck> healthCheck() {
        return ResponseEntity.ok(new DadosHealthCheck("UP", "3.0.0", Instant.now()));
    }

    public record DadosHealthCheck(String status, String versao, Instant timestamp) {}
}
