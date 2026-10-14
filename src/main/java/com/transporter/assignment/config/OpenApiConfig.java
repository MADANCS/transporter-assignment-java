package com.transporter.assignment.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Springdoc / Swagger-UI OpenAPI metadata.
 *
 * <p>Swagger UI: <a href="http://localhost:8080/swagger-ui.html">http://localhost:8080/swagger-ui.html</a><br>
 * Raw spec:     <a href="http://localhost:8080/api-docs">http://localhost:8080/api-docs</a>
 */
@Configuration
public class OpenApiConfig {

    @Value("${server.port:8080}")
    private String serverPort;

    @Bean
    public OpenAPI transporterAssignmentOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Transporter Assignment API")
                        .description("""
                                Optimally assigns transport lanes to transporters while minimising \
                                total cost, subject to a configurable maximum-transporter constraint.

                                **Workflow**
                                1. `POST /api/v1/transporters/input` – load lanes, transporters and quotes.
                                2. `POST /api/v1/transporters/assignment` – run the optimisation.

                                **Algorithm**
                                - *Phase 1*: assign each lane to its cheapest transporter (unconstrained optimum).
                                - *Phase 2*: if distinct transporters > `maxTransporters`, iteratively drop the \
                                transporter whose lanes can be re-assigned at minimum extra cost until the \
                                constraint is satisfied.
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Transporter Assignment Team")
                                .email("team@transporter.example.com"))
                        .license(new License()
                                .name("MIT")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:" + serverPort)
                                .description("Local development server")));
    }
}
