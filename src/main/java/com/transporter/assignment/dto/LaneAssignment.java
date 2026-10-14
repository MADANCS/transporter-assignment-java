package com.transporter.assignment.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * A single lane-to-transporter assignment entry in the optimised result.
 */
@Schema(description = "Assignment of one lane to one transporter")
public class LaneAssignment {

    @Schema(description = "Lane ID", example = "1")
    private Long laneId;

    @Schema(description = "ID of the assigned transporter", example = "1")
    private Long transporterId;

    // ── Builder factory ───────────────────────────────────────────

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private Long laneId;
        private Long transporterId;

        public Builder laneId(Long v)       { this.laneId = v; return this; }
        public Builder transporterId(Long v){ this.transporterId = v; return this; }

        public LaneAssignment build() {
            LaneAssignment a = new LaneAssignment();
            a.laneId = laneId;
            a.transporterId = transporterId;
            return a;
        }
    }

    // ── Getters & Setters ─────────────────────────────────────────

    public Long getLaneId()                { return laneId; }
    public void setLaneId(Long v)          { this.laneId = v; }

    public Long getTransporterId()         { return transporterId; }
    public void setTransporterId(Long v)   { this.transporterId = v; }
}
