package com.mcp.taskserver.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {
    @Value("${mcp.server.version:2025-06-18}")
    private String mcpVersion;

    @Bean
    public OpenAPI mcpOpenAPI() {
        return new OpenAPI()
                .info(new Info().title("MCP Task Server API").version(mcpVersion)
                        .description("MCP Server exposing controlled DB access for AI agents."))
                .servers(List.of(new Server().url("http://localhost:8081").description("Local")));
    }
}
