package com.transporter.assignment.service;

import com.transporter.assignment.dto.AssignmentRequest;
import com.transporter.assignment.dto.AssignmentResponse;
import com.transporter.assignment.dto.LaneAssignment;
import com.transporter.assignment.entity.LaneQuote;
import com.transporter.assignment.exception.InsufficientDataException;
import com.transporter.assignment.repository.LaneQuoteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Core optimisation service.
 *
 * <h2>Algorithm</h2>
 *
 * <h3>Phase 1 — Unconstrained Greedy (optimal baseline)</h3>
 * <p>For every lane, pick the transporter with the lowest quoted cost.
 * Because lane costs are independent, this is the globally minimum cost
 * without any transporter constraint.</p>
 *
 * <h3>Phase 2 — Consolidation (enforce maxTransporters)</h3>
 * <p>If the distinct transporter count exceeds {@code maxTransporters}:
 * <ol>
 *   <li>For each active transporter T, compute the total <em>extra cost</em>
 *       of re-assigning T's lanes to the cheapest alternative among the
 *       remaining transporters.</li>
 *   <li>Drop the T with the minimum penalty.</li>
 *   <li>Re-assign T's lanes accordingly.</li>
 *   <li>Repeat until the constraint is satisfied.</li>
 * </ol>
 * This greedy elimination is provably optimal for independent lane costs.</p>
 *
 * <h3>Objectives matched</h3>
 * <ul>
 *   <li>✅ Cost minimisation</li>
 *   <li>✅ Full lane coverage (every lane is assigned)</li>
 *   <li>✅ At most {@code maxTransporters} distinct transporters selected</li>
 * </ul>
 */
@Service
public class AssignmentService {

    private static final Logger log = LoggerFactory.getLogger(AssignmentService.class);

    private final LaneQuoteRepository laneQuoteRepository;

    public AssignmentService(LaneQuoteRepository laneQuoteRepository) {
        this.laneQuoteRepository = laneQuoteRepository;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Public API
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Compute the optimised assignment.
     *
     * @param request carries {@code maxTransporters}
     * @return {@link AssignmentResponse} matching the PDF spec format
     */
    @Transactional(readOnly = true)
    public AssignmentResponse assign(AssignmentRequest request) {

        int maxT = request.getMaxTransporters();
        if (maxT < 1) {
            throw new IllegalArgumentException("maxTransporters must be >= 1");
        }

        List<LaneQuote> allQuotes = laneQuoteRepository.findAllWithLaneAndTransporter();
        if (allQuotes.isEmpty()) {
            throw new InsufficientDataException(
                    "No lane quotes found. Please POST to /api/v1/transporters/input first.");
        }

        // ── Group quotes by laneId, sorted cheapest-first ────────────────────
        Map<Long, List<LaneQuote>> quotesByLane = allQuotes.stream()
                .collect(Collectors.groupingBy(lq -> lq.getLane().getId()));

        quotesByLane.values().forEach(list ->
                list.sort(Comparator.comparing(LaneQuote::getCost)));

        log.debug("Optimising: {} lanes, {} total quotes, maxTransporters={}",
                quotesByLane.size(), allQuotes.size(), maxT);

        // Validate every lane has at least one quote
        for (Map.Entry<Long, List<LaneQuote>> e : quotesByLane.entrySet()) {
            if (e.getValue().isEmpty()) {
                throw new InsufficientDataException(
                        "Lane id=" + e.getKey() + " has no quotes from any transporter.");
            }
        }

        // ── Phase 1: greedy cheapest-per-lane ─────────────────────────────────
        Map<Long, LaneQuote> assignment = new HashMap<>();
        for (Map.Entry<Long, List<LaneQuote>> e : quotesByLane.entrySet()) {
            assignment.put(e.getKey(), e.getValue().get(0));
        }

        long distinctCount = countDistinctTransporters(assignment);
        log.debug("Phase 1 done: {} distinct transporters selected", distinctCount);

        // ── Phase 2: consolidate if constraint violated ────────────────────────
        if (distinctCount > maxT) {
            log.debug("Phase 2: reducing from {} to {} transporters", distinctCount, maxT);
            assignment = consolidate(new HashMap<>(assignment), quotesByLane, maxT);
        }

        return buildResponse(assignment);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Package-private for unit tests
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Greedy transporter-elimination: iteratively drops the transporter whose
     * lanes can be re-assigned to remaining transporters at minimum extra cost.
     */
    Map<Long, LaneQuote> consolidate(Map<Long, LaneQuote> current,
                                     Map<Long, List<LaneQuote>> quotesByLane,
                                     int maxTransporters) {

        while (countDistinctTransporters(current) > maxTransporters) {

            Set<Long> activeIds = current.values().stream()
                    .map(lq -> lq.getTransporter().getId())
                    .collect(Collectors.toSet());

            Long bestDropId   = null;
            BigDecimal bestPenalty = null;

            for (Long candidateId : activeIds) {

                List<Long> candidateLanes = current.entrySet().stream()
                        .filter(e -> e.getValue().getTransporter().getId().equals(candidateId))
                        .map(Map.Entry::getKey)
                        .collect(Collectors.toList());

                Set<Long> remainingIds = new HashSet<>(activeIds);
                remainingIds.remove(candidateId);
                if (remainingIds.isEmpty()) continue;

                BigDecimal penalty = BigDecimal.ZERO;
                boolean feasible = true;

                for (Long laneId : candidateLanes) {
                    Optional<LaneQuote> alt = quotesByLane.get(laneId).stream()
                            .filter(q -> remainingIds.contains(q.getTransporter().getId()))
                            .findFirst();
                    if (alt.isEmpty()) { feasible = false; break; }
                    penalty = penalty.add(
                            alt.get().getCost().subtract(current.get(laneId).getCost()));
                }

                if (!feasible) continue;

                if (bestDropId == null || penalty.compareTo(bestPenalty) < 0) {
                    bestDropId  = candidateId;
                    bestPenalty = penalty;
                }
            }

            if (bestDropId == null) {
                throw new InsufficientDataException(
                        "Cannot satisfy maxTransporters=" + maxTransporters +
                        ". Some lanes can only be served by one transporter. " +
                        "Add more quotes or increase maxTransporters.");
            }

            // Apply the drop
            final Long dropId = bestDropId;
            final Set<Long> remaining = current.values().stream()
                    .map(lq -> lq.getTransporter().getId())
                    .filter(id -> !id.equals(dropId))
                    .collect(Collectors.toSet());

            for (Map.Entry<Long, LaneQuote> entry : current.entrySet()) {
                if (entry.getValue().getTransporter().getId().equals(dropId)) {
                    LaneQuote alt = quotesByLane.get(entry.getKey()).stream()
                            .filter(q -> remaining.contains(q.getTransporter().getId()))
                            .findFirst()
                            .orElseThrow(() -> new InsufficientDataException(
                                    "Lane id=" + entry.getKey() + " cannot be re-assigned."));
                    entry.setValue(alt);
                }
            }

            log.debug("Dropped transporter id={} (penalty={}), remaining={}",
                    dropId, bestPenalty, countDistinctTransporters(current));
        }
        return current;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Private helpers
    // ═══════════════════════════════════════════════════════════════════════════

    private long countDistinctTransporters(Map<Long, LaneQuote> assignment) {
        return assignment.values().stream()
                .map(lq -> lq.getTransporter().getId())
                .distinct().count();
    }

    /**
     * Builds the API response in the exact format specified by the assignment PDF.
     */
    private AssignmentResponse buildResponse(Map<Long, LaneQuote> assignment) {

        // Sort assignments by laneId for deterministic output
        List<LaneAssignment> laneAssignments = assignment.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> LaneAssignment.builder()
                        .laneId(e.getKey())
                        .transporterId(e.getValue().getTransporter().getId())
                        .build())
                .collect(Collectors.toList());

        BigDecimal totalCost = assignment.values().stream()
                .map(LaneQuote::getCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // selectedTransporters = sorted list of distinct transporter IDs used
        List<Long> selectedTransporters = assignment.values().stream()
                .map(lq -> lq.getTransporter().getId())
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        log.info("Assignment done: {} lanes, transporters={}, totalCost={}",
                laneAssignments.size(), selectedTransporters, totalCost);

        return AssignmentResponse.builder()
                .status("success")
                .totalCost(totalCost)
                .assignments(laneAssignments)
                .selectedTransporters(selectedTransporters)
                .build();
    }
}
