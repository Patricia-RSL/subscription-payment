package com.patricia.subscriptionApp.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI configuration. Access Swagger UI: /swagger-ui/index.html
 */
@Configuration
public class OpenApiConfig {

    @Value("${server.port:8080}")
    private String serverPort;

    @Bean
    public OpenAPI customOpenAPI() {
        Server local = new Server().url("http://localhost:" + serverPort).description("Local");
        Info info = new Info()
                .title("Subscription Service API")
                .version("v1")
                .description("API do desafio técnico de assinaturas. A solução prioriza regras de negócio, concorrência, agendamentos e integração assíncrona com RabbitMQ. Não há autenticação/autorização por decisão de escopo do desafio. Endpoints administrativos e utilitários de teste foram mantidos expostos apenas para desenvolvimento, demonstração e validação manual dos fluxos.");
        return new OpenAPI().servers(List.of(local)).info(info);
    }
}
