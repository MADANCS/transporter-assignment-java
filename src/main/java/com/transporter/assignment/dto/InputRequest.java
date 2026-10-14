package com.transporter.assignment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

/**
 * Request body for POST /api/v1/transporters/input.
 *
 * <p>Lanes and transporters carry client-provided numeric IDs.
 * Each transporter nests its own lane quotes.
 */
@Schema(description = "Input payload: lanes and transporters with their quoted costs per lane")
public class InputRequest {

    @NotEmpty(message = "At least one lane is required")
    @Valid
    @Schema(description = "List of lanes")
    private List<LaneDto> lanes;

    @NotEmpty(message = "At least one transporter is required")
    @Valid
    @Schema(description = "List of transporters, each carrying their lane quotes")
    private List<TransporterDto> transporters;

    // ── Getters & Setters ─────────────────────────────────────────

    public List<LaneDto> getLanes()                             { return lanes; }
    public void setLanes(List<LaneDto> v)                      { this.lanes = v; }

    public List<TransporterDto> getTransporters()              { return transporters; }
    public void setTransporters(List<TransporterDto> v)        { this.transporters = v; }

    // ══ Nested DTOs ═══════════════════════════════════════════════

    @Schema(description = "A transport lane")
    public static class LaneDto {

        @NotNull(message = "Lane id is required")
        @Schema(example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        private Long id;

        @NotNull(message = "origin is required")
        @Schema(example = "Mumbai", requiredMode = Schema.RequiredMode.REQUIRED)
        private String origin;

        @NotNull(message = "destination is required")
        @Schema(example = "Delhi", requiredMode = Schema.RequiredMode.REQUIRED)
        private String destination;

        public Long getId()                  { return id; }
        public void setId(Long v)            { this.id = v; }

        public String getOrigin()            { return origin; }
        public void setOrigin(String v)      { this.origin = v; }

        public String getDestination()       { return destination; }
        public void setDestination(String v) { this.destination = v; }
    }

    @Schema(description = "A transporter with their quoted costs per lane")
    public static class TransporterDto {

        @NotNull(message = "Transporter id is required")
        @Schema(example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        private Long id;

        @NotNull(message = "Transporter name is required")
        @Schema(example = "TransporterT1", requiredMode = Schema.RequiredMode.REQUIRED)
        private String name;

        @NotEmpty(message = "laneQuotes must not be empty for a transporter")
        @Valid
        @Schema(description = "Quotes offered by this transporter for specific lanes")
        private List<LaneQuoteDto> laneQuotes;

        public Long getId()                          { return id; }
        public void setId(Long v)                    { this.id = v; }

        public String getName()                      { return name; }
        public void setName(String v)                { this.name = v; }

        public List<LaneQuoteDto> getLaneQuotes()    { return laneQuotes; }
        public void setLaneQuotes(List<LaneQuoteDto> v) { this.laneQuotes = v; }
    }

    @Schema(description = "A cost quote for a specific lane")
    public static class LaneQuoteDto {

        @NotNull(message = "laneId is required in a quote")
        @Schema(description = "ID of the lane being quoted", example = "1",
                requiredMode = Schema.RequiredMode.REQUIRED)
        private Long laneId;

        @NotNull(message = "quote (cost) is required")
        @Schema(description = "Cost quoted for this lane", example = "5000",
                requiredMode = Schema.RequiredMode.REQUIRED)
        private BigDecimal quote;

        public Long getLaneId()            { return laneId; }
        public void setLaneId(Long v)      { this.laneId = v; }

        public BigDecimal getQuote()       { return quote; }
        public void setQuote(BigDecimal v) { this.quote = v; }
    }
}
