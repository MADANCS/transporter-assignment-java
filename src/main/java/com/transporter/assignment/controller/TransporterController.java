package com.transporter.assignment.controller;

import com.transporter.assignment.dto.AssignmentRequest;
import com.transporter.assignment.dto.AssignmentResponse;
import com.transporter.assignment.dto.ErrorResponse;
import com.transporter.assignment.dto.InputRequest;
import com.transporter.assignment.service.AssignmentService;
import com.transporter.assignment.service.TransporterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST controller exposing the two transporter-assignment endpoints.
 */
@RestController
@RequestMapping("/api/v1/transporters")
@Tag(
    name = "Transporter Assignment",
    description = "Save input data (lanes, transporters, quotes) and run the cost-minimising assignment."
)
public class TransporterController {

    private static final Logger log = LoggerFactory.getLogger(TransporterController.class);

    private final TransporterService transporterService;
    private final AssignmentService  assignmentService;

    public TransporterController(TransporterService transporterService,
                                 AssignmentService assignmentService) {
        this.transporterService = transporterService;
        this.assignmentService  = assignmentService;
    }

    // ══════════════════════════════════════════════════════════════════════════
    // POST /api/v1/transporters/input
    // ══════════════════════════════════════════════════════════════════════════

    @PostMapping("/input")
    @Operation(
        summary = "Save input data",
        description = """
            Persists lanes, transporters and their lane quotes to the database.

            **Important**: each call **replaces** all previously stored data.

            Each transporter carries its own `laneQuotes` array with `laneId` (matching a lane's `id`)
            and `quote` (the cost offered for that lane).
            """
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
        required = true,
        content = @Content(
            mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = InputRequest.class),
            examples = @ExampleObject(
                name = "5 lanes, 7 transporters",
                summary = "Sample input with nested laneQuotes per transporter (Test Case 1)",
                value = """
                    {
                      "lanes": [
                        {"id": 1, "origin": "Mumbai",    "destination": "Delhi"},
                        {"id": 2, "origin": "Delhi",     "destination": "Bangalore"},
                        {"id": 3, "origin": "Chennai",   "destination": "Kolkata"},
                        {"id": 4, "origin": "Pune",      "destination": "Hyderabad"},
                        {"id": 5, "origin": "Ahmedabad", "destination": "Jaipur"}
                      ],
                      "transporters": [
                        {"id": 1, "name": "Transporter T1", "laneQuotes": [{"laneId": 1, "quote": 20835}, {"laneId": 2, "quote": 10512}, {"laneId": 3, "quote": 22105}, {"laneId": 4, "quote": 42481}, {"laneId": 5, "quote": 19862}]},
                        {"id": 2, "name": "Transporter T2", "laneQuotes": [{"laneId": 1, "quote": 48844}, {"laneId": 2, "quote": 31326}, {"laneId": 3, "quote": 18640}, {"laneId": 4, "quote": 45828}, {"laneId": 5, "quote": 18297}]},
                        {"id": 3, "name": "Transporter T3", "laneQuotes": [{"laneId": 1, "quote": 39020}, {"laneId": 2, "quote": 20648}, {"laneId": 3, "quote": 31438}, {"laneId": 4, "quote": 36447}, {"laneId": 5, "quote": 12789}]},
                        {"id": 4, "name": "Transporter T4", "laneQuotes": [{"laneId": 1, "quote": 14400}, {"laneId": 2, "quote": 44514}, {"laneId": 3, "quote": 14316}, {"laneId": 4, "quote": 10678}, {"laneId": 5, "quote": 13032}]},
                        {"id": 5, "name": "Transporter T5", "laneQuotes": [{"laneId": 1, "quote": 11601}, {"laneId": 2, "quote": 19760}, {"laneId": 3, "quote": 40870}, {"laneId": 4, "quote": 20635}, {"laneId": 5, "quote": 26421}]},
                        {"id": 6, "name": "Transporter T6", "laneQuotes": [{"laneId": 1, "quote": 35095}, {"laneId": 2, "quote": 12494}, {"laneId": 3, "quote": 17808}, {"laneId": 4, "quote": 36210}, {"laneId": 5, "quote": 39444}]},
                        {"id": 7, "name": "Transporter T7", "laneQuotes": [{"laneId": 1, "quote": 26070}, {"laneId": 2, "quote": 41098}, {"laneId": 3, "quote": 20932}, {"laneId": 4, "quote": 16897}, {"laneId": 5, "quote": 27938}]}
                      ]
                    }
                    """
            )
        )
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Data saved successfully",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                examples = @ExampleObject(value = """
                    { "status": "success", "message": "Input data saved successfully." }
                    """))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Validation error – missing or invalid fields",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public ResponseEntity<Map<String, String>> saveInput(
            @Valid @RequestBody InputRequest request) {

        log.info("POST /input – {} lanes, {} transporters",
                request.getLanes().size(),
                request.getTransporters().size());

        String message = transporterService.saveInput(request);
        return ResponseEntity.ok(Map.of("status", "success", "message", message));
    }

    // ══════════════════════════════════════════════════════════════════════════
    // POST /api/v1/transporters/assignment
    // ══════════════════════════════════════════════════════════════════════════

    @PostMapping("/assignment")
    @Operation(
        summary = "Run assignment optimisation",
        description = """
            Runs the two-phase greedy optimisation:

            1. **Phase 1** – assign each lane to its cheapest available transporter.
            2. **Phase 2** – if the number of distinct transporters exceeds `maxTransporters`,
               iteratively drop the transporter whose lanes can be re-assigned at the lowest
               additional cost, until the constraint is met.

            Returns the full assignment, total cost, and selected transporters.

            > Call `POST /input` first to load the data before running this endpoint.
            """
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
        required = true,
        content = @Content(
            mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = AssignmentRequest.class),
            examples = @ExampleObject(
                name = "maxTransporters = 2",
                value = """
                    { "maxTransporters": 2 }
                    """
            )
        )
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Assignment computed successfully",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = AssignmentResponse.class),
                examples = @ExampleObject(value = """
                    {
                      "status": "success",
                      "totalCost": 75000,
                      "assignments": [
                        {"laneId": 1, "transporterId": 4},
                        {"laneId": 2, "transporterId": 6},
                        {"laneId": 3, "transporterId": 2},
                        {"laneId": 4, "transporterId": 4},
                        {"laneId": 5, "transporterId": 3}
                      ],
                      "selectedTransporters": [4, 6, 3]
                    }
                    """))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "maxTransporters is missing or < 1",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "422",
            description = "No data loaded, or constraint is infeasible (some lanes can only be served by one transporter)",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public ResponseEntity<AssignmentResponse> computeAssignment(
            @Valid @RequestBody AssignmentRequest request) {

        log.info("POST /assignment – maxTransporters={}", request.getMaxTransporters());

        AssignmentResponse response = assignmentService.assign(request);
        return ResponseEntity.ok(response);
    }
}
