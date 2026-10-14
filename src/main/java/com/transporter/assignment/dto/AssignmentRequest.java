package com.transporter.assignment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Request body for POST /api/v1/transporters/assignment.
 */
@Schema(description = "Parameters for the assignment optimisation run")
public class AssignmentRequest {

    @NotNull(message = "maxTransporters is required")
    @Min(value = 1, message = "maxTransporters must be at least 1")
    @Schema(description = "Maximum number of distinct transporters that may be selected",
            example = "3", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer maxTransporters;

    public Integer getMaxTransporters()              { return maxTransporters; }
    public void setMaxTransporters(Integer v)        { this.maxTransporters = v; }
}
