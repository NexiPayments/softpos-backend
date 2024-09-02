package com.example.pizzapaybackend;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import java.util.ArrayList;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiInfo {

    @Bean
    OpenAPI customOpenAPI() {
        final Info info = new Info();
        info.title("Pizza Pay Demo App");

        final String securitySchemeName = "appLogin";
        final OpenAPI oapi = new OpenAPI().components(new Components());
        oapi.addSecurityItem(new SecurityRequirement().addList(securitySchemeName)).components(
            new Components().addSecuritySchemes(
                securitySchemeName,
                new SecurityScheme().name(securitySchemeName).type(SecurityScheme.Type.HTTP).scheme("bearer")
            )
        );
        oapi.info(info);
        oapi.servers(new ArrayList<>());
        oapi.addServersItem(new Server().url("/"));
        return oapi;
    }

}
