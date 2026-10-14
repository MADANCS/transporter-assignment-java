package com.transporter.assignment.entity;

import jakarta.persistence.*;

/**
 * Represents a transport lane from one location to another.
 * The ID is client-supplied (from the input request), not auto-generated.
 */
@Entity
@Table(name = "lanes")
public class Lane {

    /**
     * Client-provided ID (matches the id field in the API request).
     * Not auto-generated so we preserve the caller's identifiers.
     */
    @Id
    private Long id;

    @Column(nullable = false)
    private String origin;

    @Column(nullable = false)
    private String destination;

    // ── Constructors ─────────────────────────────────────────────

    public Lane() {}

    public Lane(Long id, String origin, String destination) {
        this.id = id;
        this.origin = origin;
        this.destination = destination;
    }

    // ── Builder factory ───────────────────────────────────────────

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private Long id;
        private String origin;
        private String destination;

        public Builder id(Long id)           { this.id = id; return this; }
        public Builder origin(String v)      { this.origin = v; return this; }
        public Builder destination(String v) { this.destination = v; return this; }
        public Lane build()                  { return new Lane(id, origin, destination); }
    }

    // ── Getters & Setters ─────────────────────────────────────────

    public Long getId()                  { return id; }
    public void setId(Long id)           { this.id = id; }

    public String getOrigin()            { return origin; }
    public void setOrigin(String v)      { this.origin = v; }

    public String getDestination()           { return destination; }
    public void setDestination(String v)     { this.destination = v; }
}
