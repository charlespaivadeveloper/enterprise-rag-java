package dev.charlespaiva.rag.framework.web;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI enterpriseRagOpenApi() {
        return new OpenAPI()
            .info(new Info()
                .title("Enterprise RAG Platform API")
                .version("0.1.1")
                .description("Multi-tenant RAG platform — hybrid semantic + lexical search over private knowledge bases.")
                .contact(new Contact()
                    .name("Charles Paiva")
                    .email("paiva.charles@gmail.com")))
            .components(new Components()
                .addSecuritySchemes("ApiKeyAuth",
                    new SecurityScheme()
                        .type(SecurityScheme.Type.APIKEY)
                        .in(SecurityScheme.In.HEADER)
                        .name("X-Api-Key")))
            .addSecurityItem(new SecurityRequirement().addList("ApiKeyAuth"));
    }
}
