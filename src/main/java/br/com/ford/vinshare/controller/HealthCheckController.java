package br.com.ford.vinshare.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/health-check")
@Tag(name = "Health Check", description = "Endpoint para verificação de saúde da API")
public class HealthCheckController {

    @GetMapping
    @Operation(summary = "Health check", description = "Retorna OK se a API estiver funcionando")
    public String healthCheck() {
        return "OK";
    }
}