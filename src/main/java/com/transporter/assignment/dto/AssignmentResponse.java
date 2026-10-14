package com.transporter.assignment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.List;

/**
 * Response for POST /api/v1/transporters/assignment.
 *
 * <p>Matches the spec exactly:
 * <pre>
 * {
 *   "status": "success",
 *   "totalCost": 29000,
 *   "assignments": [{"laneId":1,"transporterId":1}, ...],
 *   "selectedTransporters": [1, 2, 3]
 * }
 * </pre>
 */
@Schema(description = "Result of the transporter assignment optimisation")
public class AssignmentResponse {

    @Schema(description = "Result status", example = "success")
    private String status;

    @Schema(description = "Total optimised cost", example = "29000")
    private BigDecimal totalCost;

    @Schema(description = "Per-lane assignment: which transporter covers each lane")
    private List<LaneAssignment> assignments;

    @Schema(description = "IDs of the selected transporters", example = "[1, 2, 3]")
    private List<Long> selectedTransporters;

    // ── Builder factory ───────────────────────────────────────────

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private String status;
        private BigDecimal totalCost;
        private List<LaneAssignment> assignments;
        private List<Long> selectedTransporters;

        public Builder status(String v)                       { this.status = v; return this; }
        public Builder totalCost(BigDecimal v)                { this.totalCost = v; return this; }
        public Builder assignments(List<LaneAssignment> v)    { this.assignments = v; return this; }
        public Builder selectedTransporters(List<Long> v)     { this.selectedTransporters = v; return this; }

        public AssignmentResponse build() {
            AssignmentResponse r = new AssignmentResponse();
            r.status = status;
            r.totalCost = totalCost;
            r.assignments = assignments;
            r.selectedTransporters = selectedTransporters;
            return r;
        }
    }

    // ── Getters & Setters ─────────────────────────────────────────

    public String getStatus()                           { return status; }
    public void setStatus(String v)                     { this.status = v; }

    public BigDecimal getTotalCost()                    { return totalCost; }
    public void setTotalCost(BigDecimal v)              { this.totalCost = v; }

    public List<LaneAssignment> getAssignments()        { return assignments; }
    public void setAssignments(List<LaneAssignment> v)  { this.assignments = v; }

    public List<Long> getSelectedTransporters()              { return selectedTransporters; }
    public void setSelectedTransporters(List<Long> v)        { this.selectedTransporters = v; }
}
