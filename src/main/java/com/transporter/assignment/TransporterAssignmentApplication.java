package com.transporter.assignment;

import com.transporter.assignment.config.RenderDatabaseUrlProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the Transporter Assignment application.
 *
 * <p>Once started, the Swagger UI is available at:
 * <a href="http://localhost:8080/swagger-ui.html">http://localhost:8080/swagger-ui.html</a>
 */
@SpringBootApplication
public class TransporterAssignmentApplication {

    public static void main(String[] args) {
        // Pre-configure JDBC URL and credentials before Spring Boot starts
        RenderDatabaseUrlProcessor.configureSystemProperties();
        SpringApplication.run(TransporterAssignmentApplication.class, args);
    }
}
