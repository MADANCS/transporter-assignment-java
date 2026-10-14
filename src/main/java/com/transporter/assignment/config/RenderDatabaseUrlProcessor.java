package com.transporter.assignment.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.HashMap;
import java.util.Map;

/**
 * Converts Render's DATABASE_URL (postgres:// or postgresql://user:pass@host:port/db)
 * to Spring Boot's JDBC URL format (jdbc:postgresql://host:port/db).
 * Runs before the application context is created.
 */
public class RenderDatabaseUrlProcessor implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment,
                                       SpringApplication application) {
        String rawUrl = System.getenv("DATABASE_URL");
        if (rawUrl == null || rawUrl.isBlank()) {
            System.out.println("[RenderDatabaseUrlProcessor] DATABASE_URL not set, skipping.");
            return;
        }

        System.out.println("[RenderDatabaseUrlProcessor] Processing DATABASE_URL scheme: "
                + rawUrl.substring(0, Math.min(rawUrl.indexOf('@') > 0 ? rawUrl.indexOf('@') : 30, rawUrl.length())));

        // If already a JDBC URL, use as-is
        if (rawUrl.startsWith("jdbc:")) {
            System.out.println("[RenderDatabaseUrlProcessor] Already a JDBC URL, using directly.");
            Map<String, Object> props = new HashMap<>();
            props.put("spring.datasource.url", rawUrl);
            environment.getPropertySources()
                    .addFirst(new MapPropertySource("renderDatabaseUrl", props));
            return;
        }

        // Handle both postgres:// and postgresql:// schemes (Render uses both)
        try {
            // Strip the scheme: postgres:// or postgresql://
            String withoutScheme = rawUrl.replaceFirst("^postgre(?:sql)?://", "");

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

            System.out.println("[RenderDatabaseUrlProcessor] Converted to JDBC URL: jdbc:postgresql://" + hostAndPort + "/" + dbName + "?sslmode=require");

            Map<String, Object> props = new HashMap<>();
            props.put("spring.datasource.url", jdbcUrl);
            props.put("spring.datasource.username", user);
            props.put("spring.datasource.password", pass);

            environment.getPropertySources()
                    .addFirst(new MapPropertySource("renderDatabaseUrl", props));

        } catch (Exception e) {
            System.err.println("[RenderDatabaseUrlProcessor] Failed to parse DATABASE_URL: " + e.getMessage());
            System.err.println("[RenderDatabaseUrlProcessor] Raw URL (masked): " + rawUrl.replaceAll(":([^@]+)@", ":***@"));
        }
    }
}
