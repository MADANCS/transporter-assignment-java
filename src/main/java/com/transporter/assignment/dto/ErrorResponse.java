package com.transporter.assignment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Map;

/**
 * Standard error envelope returned by the global exception handler.
 */
@Schema(description = "Standard API error response")
public class ErrorResponse {

    @Schema(description = "ISO-8601 timestamp of when the error occurred",
            example = "2024-01-15T10:30:00.000")
    private String timestamp;

    @Schema(description = "HTTP status code", example = "400")
    private int status;

    @Schema(description = "HTTP reason phrase", example = "Bad Request")
    private String error;

    @Schema(description = "Human-readable error message",
            example = "maxTransporters must be at least 1")
    private String message;

    @Schema(description = "Per-field validation errors (only present for 400 validation failures)")
    private Map<String, String> details;

    // ── Getters & Setters ─────────────────────────────────────────

    public String getTimestamp()             { return timestamp; }
    public void setTimestamp(String v)       { this.timestamp = v; }

    public int getStatus()                   { return status; }
    public void setStatus(int v)             { this.status = v; }

    public String getError()                 { return error; }
    public void setError(String v)           { this.error = v; }

    public String getMessage()               { return message; }
    public void setMessage(String v)         { this.message = v; }

    public Map<String, String> getDetails()  { return details; }
    public void setDetails(Map<String, String> v) { this.details = v; }
}
