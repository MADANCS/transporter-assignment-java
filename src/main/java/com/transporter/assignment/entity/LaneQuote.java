package com.transporter.assignment.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

/**
 * Holds the cost quoted by a specific transporter for a specific lane.
 * A unique constraint on (lane_id, transporter_id) ensures one quote per pair.
 */
@Entity
@Table(
    name = "lane_quotes",
    uniqueConstraints = @UniqueConstraint(columnNames = {"lane_id", "transporter_id"})
)
public class LaneQuote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "lane_id", nullable = false)
    private Lane lane;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "transporter_id", nullable = false)
    private Transporter transporter;

    /**
     * Cost the transporter quoted for this lane.
     * BigDecimal ensures monetary precision.
     */
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal cost;

    // ── Constructors ─────────────────────────────────────────────

    public LaneQuote() {}

    public LaneQuote(Long id, Lane lane, Transporter transporter, BigDecimal cost) {
        this.id = id;
        this.lane = lane;
        this.transporter = transporter;
        this.cost = cost;
    }

    // ── Builder factory ───────────────────────────────────────────

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private Long id;
        private Lane lane;
        private Transporter transporter;
        private BigDecimal cost;

        public Builder id(Long id)                   { this.id = id; return this; }
        public Builder lane(Lane v)                  { this.lane = v; return this; }
        public Builder transporter(Transporter v)    { this.transporter = v; return this; }
        public Builder cost(BigDecimal v)            { this.cost = v; return this; }
        public LaneQuote build() {
            return new LaneQuote(id, lane, transporter, cost);
        }
    }

    // ── Getters & Setters ─────────────────────────────────────────

    public Long getId()                      { return id; }
    public void setId(Long id)               { this.id = id; }

    public Lane getLane()                    { return lane; }
    public void setLane(Lane v)              { this.lane = v; }

    public Transporter getTransporter()      { return transporter; }
    public void setTransporter(Transporter v){ this.transporter = v; }

    public BigDecimal getCost()              { return cost; }
    public void setCost(BigDecimal v)        { this.cost = v; }
}
