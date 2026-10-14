package com.transporter.assignment.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.HashMap;
import java.util.Map;

/**
 * Converts Render's DATABASE_URL (postgres:// or postgresql://user:pass@host:port/db)
 * to Spring Boot's JDBC URL format (jdbc:postgresql://host:port/db?sslmode=prefer).
 *
 * Runs both from main() before Spring starts and as an EnvironmentPostProcessor.
 */
public class RenderDatabaseUrlProcessor implements EnvironmentPostProcessor {

    public static void configureSystemProperties() {
        String rawUrl = System.getenv("DATABASE_URL");
        if (rawUrl == null || rawUrl.isBlank()) {
            return;
        }

        try {
            Map<String, String> resolved = parseDatabaseUrl(rawUrl);
            for (Map.Entry<String, String> entry : resolved.entrySet()) {
                System.setProperty(entry.getKey(), entry.getValue());
            }
            System.out.println("[RenderDatabaseUrlProcessor] Configured system property spring.datasource.url: "
                    + resolved.get("spring.datasource.url"));
        } catch (Exception e) {
            System.err.println("[RenderDatabaseUrlProcessor] Error configuring system properties: " + e.getMessage());
        }
    }

    public static Map<String, String> parseDatabaseUrl(String rawUrl) {
        Map<String, String> props = new HashMap<>();

        if (rawUrl.startsWith("jdbc:")) {
            props.put("spring.datasource.url", rawUrl);
            return props;
        }

        String withoutScheme = rawUrl.replaceFirst("^postgre(?:sql)?://", "");

        String user = "";
        String pass = "";
        String hostPortDb = withoutScheme;

        if (withoutScheme.contains("@")) {
            String[] atParts = withoutScheme.split("@", 2);
            String[] userPass = atParts[0].split(":", 2);
            user = userPass[0];
            pass = userPass.length > 1 ? userPass[1] : "";
            hostPortDb = atParts[1];
        }

        String envUser = System.getenv("DB_USERNAME");
        String envPass = System.getenv("DB_PASSWORD");
        if (envUser != null && !envUser.isBlank()) {
            user = envUser;
        }
        if (envPass != null && !envPass.isBlank()) {
            pass = envPass;
        }

        String[] hostPort = hostPortDb.split("/", 2);
        String hostAndPort = hostPort[0];
        String dbNameWithParams = hostPort.length > 1 ? hostPort[1] : "transporter_db";

        String cleanDbName = dbNameWithParams.contains("?")
                ? dbNameWithParams.substring(0, dbNameWithParams.indexOf('?'))
                : dbNameWithParams;
        if (cleanDbName.isBlank()) {
            cleanDbName = "transporter_db";
        }

        String jdbcUrl = "jdbc:postgresql://" + hostAndPort + "/" + cleanDbName + "?sslmode=prefer";

        props.put("spring.datasource.url", jdbcUrl);
        props.put("SPRING_DATASOURCE_URL", jdbcUrl);
        props.put("JDBC_DATABASE_URL", jdbcUrl);
        if (!user.isBlank()) {
            props.put("spring.datasource.username", user);
            props.put("SPRING_DATASOURCE_USERNAME", user);
        }
        if (!pass.isBlank()) {
            props.put("spring.datasource.password", pass);
            props.put("SPRING_DATASOURCE_PASSWORD", pass);
        }

        return props;
    }

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment,
                                       SpringApplication application) {
        String rawUrl = System.getenv("DATABASE_URL");
        if (rawUrl == null || rawUrl.isBlank()) {
            return;
        }

        try {
            Map<String, String> parsed = parseDatabaseUrl(rawUrl);
            Map<String, Object> props = new HashMap<>(parsed);
            environment.getPropertySources()
                    .addFirst(new MapPropertySource("renderDatabaseUrl", props));
            System.out.println("[RenderDatabaseUrlProcessor] EnvironmentPostProcessor set spring.datasource.url: "
                    + parsed.get("spring.datasource.url"));
        } catch (Exception e) {
            System.err.println("[RenderDatabaseUrlProcessor] PostProcessor failed: " + e.getMessage());
        }
    }
}
