package ao.saas.faturacao.config;

import io.swagger.v3.oas.models.*;
import io.swagger.v3.oas.models.info.*;
import io.swagger.v3.oas.models.security.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("SaaS Faturação AGT — API")
                .description("Sistema de Faturação para Angola com integração AGT\n\n" +
                             "**Credenciais demo:** admin@empresa-demo.ao / Admin@123")
                .version("1.0.0")
                .contact(new Contact().name("SaaS Faturação").email("suporte@saas-faturacao.ao"))
                .license(new License().name("Proprietário")))
            .addSecurityItem(new SecurityRequirement().addList("Bearer"))
            .components(new Components()
                .addSecuritySchemes("Bearer", new SecurityScheme()
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")
                    .description("Insira o token JWT obtido em /v1/auth/login")));
    }
}
