package com.diabetes.business_back.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenAPIConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Sistema de Predicción de Diabetes API")
                        .version("1.0.0")
                        .description("API REST para el sistema de predicción de tipos de diabetes utilizando Machine Learning")
                        .contact(new Contact()
                                .name("Equipo de Desarrollo")
                                .email("equipo@diabetes.com")
                                .url("https://diabetes-project.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080/api")
                                .description("Servidor de Desarrollo"),
                        new Server()
                                .url("https://api.diabetes.com")
                                .description("Servidor de Producción")
                ));
    }
}
