package com.transporter.assignment.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.HashMap;
import java.util.Map;

/**
 * Converts Render's DATABASE_URL (postgres://user:pass@host:port/db)
 * to Spring Boot's JDBC URL format (jdbc:postgresql://host:port/db).
 * Runs before the application context is created.
 */
public class RenderDatabaseUrlProcessor implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment,
                                       SpringApplication application) {
        String rawUrl = System.getenv("DATABASE_URL");
        if (rawUrl == null || rawUrl.isBlank()) return;

        // Render format: postgres://user:password@host:port/dbname
        try {
            // Strip the postgres:// scheme
            String withoutScheme = rawUrl.replaceFirst("^postgres://", "");

            // Split user:pass@host:port/db
            String[] atParts = withoutScheme.split("@", 2);
            String[] userPass = atParts[0].split(":", 2);
            String user = userPass[0];
            String pass = userPass.length > 1 ? userPass[1] : "";

            String hostPortDb = atParts[1];
            // hostPortDb = host:port/dbname
            String[] hostPort = hostPortDb.split("/", 2);
            String hostAndPort = hostPort[0];
            String dbName = hostPort.length > 1 ? hostPort[1] : "transporter_db";

            String jdbcUrl = "jdbc:postgresql://" + hostAndPort + "/" + dbName
                    + "?sslmode=require";

            Map<String, Object> props = new HashMap<>();
            props.put("spring.datasource.url", jdbcUrl);
            props.put("spring.datasource.username", user);
            props.put("spring.datasource.password", pass);

            environment.getPropertySources()
                    .addFirst(new MapPropertySource("renderDatabaseUrl", props));

        } catch (Exception e) {
            // Log but don't fail — fallback to application.yml values
            System.err.println("[RenderDatabaseUrlProcessor] Failed to parse DATABASE_URL: " + e.getMessage());
        }
    }
}
