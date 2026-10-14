package com.transporter.assignment.entity;

import jakarta.persistence.*;

/**
 * Represents a transporter (carrier) capable of serving lanes.
 * The ID is client-supplied (from the input request), not auto-generated.
 */
@Entity
@Table(name = "transporters")
public class Transporter {

    /**
     * Client-provided ID (matches the id field in the API request).
     */
    @Id
    private Long id;

    @Column(nullable = false)
    private String name;

    // ── Constructors ─────────────────────────────────────────────

    public Transporter() {}

    public Transporter(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    // ── Builder factory ───────────────────────────────────────────

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private Long id;
        private String name;

        public Builder id(Long id)    { this.id = id; return this; }
        public Builder name(String v) { this.name = v; return this; }
        public Transporter build()    { return new Transporter(id, name); }
    }

    // ── Getters & Setters ─────────────────────────────────────────

    public Long getId()           { return id; }
    public void setId(Long id)    { this.id = id; }

    public String getName()       { return name; }
    public void setName(String v) { this.name = v; }
}
