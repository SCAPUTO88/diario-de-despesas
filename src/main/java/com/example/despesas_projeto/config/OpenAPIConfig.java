package com.example.despesas_projeto.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenAPIConfig {

    @Value("${server.port}")
    private String serverPort;

    @Bean
    public OpenAPI myOpenApi() {
        Server devServer = new Server();
        devServer.setUrl("http://localhost:" + serverPort);
        devServer.setDescription("Development server");

        Contact contact = new Contact();
        contact.setName("Diario de Despesas API");
        contact.setUrl("https://github.com/SCAPUTO88/diario-de-despesas");

        Info info = new Info()
                .title("API de Gerenciamento de Despesas")
                .version("1.0")
                .contact(contact)
                .description("Esta API permite o gerenciamento completo de transações financeiras, " +
                        "incluindo receitas e despesas, com suporte a categorização, tags e análises financeiras.");

        return new OpenAPI()
                .info(info)
                .servers(List.of(devServer));

    }
}
