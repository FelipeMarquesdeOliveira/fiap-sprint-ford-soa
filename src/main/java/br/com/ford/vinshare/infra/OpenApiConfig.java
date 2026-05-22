package br.com.ford.vinshare.infra;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("VIN Share API - Ford FIAP 2026")
                        .version("1.0.0")
                        .description("API RESTful para análise e predição de VIN Share na rede de concessionárias Ford. " +
                                "Permite gerenciar concessionárias, veículos, clientes e serviços, além de calcular " +
                                "indicadores de retenção de clientes na rede oficial de manutenção.")
                        .contact(new Contact()
                                .name("Ford & FIAP")
                                .email("fiap@ford.com"))
                        .license(new License()
                                .name("Ford Internal Use")
                                .url("https://www.ford.com.br")));
    }
}