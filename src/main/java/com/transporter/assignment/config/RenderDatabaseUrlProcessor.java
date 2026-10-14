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
            String user;
            String pass;

            if (atParts.length == 2) {
                String[] userPass = atParts[0].split(":", 2);
                user = userPass[0];
                pass = userPass.length > 1 ? userPass[1] : "";
            } else {
                user = "";
                pass = "";
            }

            // Prefer explicit Render DB_USERNAME / DB_PASSWORD env vars if provided
            String envUser = System.getenv("DB_USERNAME");
            String envPass = System.getenv("DB_PASSWORD");
            if (envUser != null && !envUser.isBlank()) {
                user = envUser;
            }
            if (envPass != null && !envPass.isBlank()) {
                pass = envPass;
            }

            String hostPortDb = atParts.length == 2 ? atParts[1] : atParts[0];
            // hostPortDb = host:port/dbname or host:port/dbname?params
            String[] hostPort = hostPortDb.split("/", 2);
            String hostAndPort = hostPort[0];
            String dbNameWithParams = hostPort.length > 1 ? hostPort[1] : "transporter_db";

            // Strip existing query params from dbName
            String cleanDbName = dbNameWithParams.contains("?")
                    ? dbNameWithParams.substring(0, dbNameWithParams.indexOf('?'))
                    : dbNameWithParams;
            if (cleanDbName.isBlank()) {
                cleanDbName = "transporter_db";
            }

            // sslmode=prefer works for both internal private networks (no SSL) and external connections (SSL)
            String jdbcUrl = "jdbc:postgresql://" + hostAndPort + "/" + cleanDbName + "?sslmode=prefer";

            System.out.println("[RenderDatabaseUrlProcessor] Converted to JDBC URL: " + jdbcUrl);
            System.out.println("[RenderDatabaseUrlProcessor] Using username: " + user);

            Map<String, Object> props = new HashMap<>();
            props.put("spring.datasource.url", jdbcUrl);
            if (!user.isBlank()) {
                props.put("spring.datasource.username", user);
            }
            if (!pass.isBlank()) {
                props.put("spring.datasource.password", pass);
            }

            environment.getPropertySources()
                    .addFirst(new MapPropertySource("renderDatabaseUrl", props));

        } catch (Exception e) {
            System.err.println("[RenderDatabaseUrlProcessor] Failed to parse DATABASE_URL: " + e.getMessage());
            System.err.println("[RenderDatabaseUrlProcessor] Raw URL (masked): " + rawUrl.replaceAll(":([^@]+)@", ":***@"));
        }
    }
}
