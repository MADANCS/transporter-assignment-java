package com.transporter.assignment.service;

import com.transporter.assignment.dto.AssignmentRequest;
import com.transporter.assignment.dto.AssignmentResponse;
import com.transporter.assignment.dto.LaneAssignment;
import com.transporter.assignment.entity.Lane;
import com.transporter.assignment.entity.LaneQuote;
import com.transporter.assignment.entity.Transporter;
import com.transporter.assignment.exception.InsufficientDataException;
import com.transporter.assignment.repository.LaneQuoteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link AssignmentService}.
 *
 * All external dependencies are mocked; only the pure optimisation algorithm is tested.
 *
 * <p>Test cases include the exact scenarios from the PDF assignment specification.</p>
 */
@ExtendWith(MockitoExtension.class)
class AssignmentServiceTest {

    @Mock
    private LaneQuoteRepository laneQuoteRepository;

    @InjectMocks
    private AssignmentService assignmentService;

    // ── Fixtures ──────────────────────────────────────────────────────────────

    private Lane lane1, lane2, lane3, lane4, lane5;
    private Transporter t1, t2, t3, t4, t5, t6, t7;

    @BeforeEach
    void setUp() {
        lane1 = lane(1L, "Mumbai",     "Delhi");
        lane2 = lane(2L, "Delhi",      "Bangalore");
        lane3 = lane(3L, "Chennai",    "Kolkata");
        lane4 = lane(4L, "Pune",       "Hyderabad");
        lane5 = lane(5L, "Ahmedabad",  "Jaipur");

        t1 = transporter(1L, "TransporterT1");
        t2 = transporter(2L, "TransporterT2");
        t3 = transporter(3L, "TransporterT3");
        t4 = transporter(4L, "TransporterT4");
        t5 = transporter(5L, "TransporterT5");
        t6 = transporter(6L, "TransporterT6");
        t7 = transporter(7L, "TransporterT7");
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Lane lane(Long id, String origin, String dest) {
        return Lane.builder().id(id).origin(origin).destination(dest).build();
    }

    private Transporter transporter(Long id, String name) {
        return Transporter.builder().id(id).name(name).build();
    }

    private LaneQuote quote(Lane lane, Transporter transporter, String cost) {
        return LaneQuote.builder()
                .id((lane.getId() * 10) + transporter.getId())
                .lane(lane)
                .transporter(transporter)
                .cost(new BigDecimal(cost))
                .build();
    }

    private AssignmentRequest req(int maxTransporters) {
        AssignmentRequest r = new AssignmentRequest();
        r.setMaxTransporters(maxTransporters);
        return r;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // PDF Test Case 1 – 5 lanes, 7 transporters, maxTransporters=3
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("PDF Test Case 1 – 5 lanes, 7 transporters, maxTransporters=3")
    class PdfTestCase1 {

        /**
         * Exact quote matrix from the PDF Test Case 1.
         * T1: L1=20835, L2=10512, L3=22105, L4=42481, L5=19862
         * T2: L1=48844, L2=31326, L3=18640, L4=45828, L5=18297
         * T3: L1=39020, L2=20648, L3=31438, L4=36447, L5=12789
         * T4: L1=14400, L2=44514, L3=14316, L4=10678, L5=13032
         * T5: L1=11601, L2=19760, L3=40870, L4=20635, L5=26421
         * T6: L1=35095, L2=12494, L3=17808, L4=36210, L5=39444
         * T7: L1=26070, L2=41098, L3=20932, L4=16897, L5=27938
         */
        private List<LaneQuote> buildQuotes() {
            return List.of(
                quote(lane1,t1,"20835"), quote(lane1,t2,"48844"), quote(lane1,t3,"39020"),
                quote(lane1,t4,"14400"), quote(lane1,t5,"11601"), quote(lane1,t6,"35095"),
                quote(lane1,t7,"26070"),

                quote(lane2,t1,"10512"), quote(lane2,t2,"31326"), quote(lane2,t3,"20648"),
                quote(lane2,t4,"44514"), quote(lane2,t5,"19760"), quote(lane2,t6,"12494"),
                quote(lane2,t7,"41098"),

                quote(lane3,t1,"22105"), quote(lane3,t2,"18640"), quote(lane3,t3,"31438"),
                quote(lane3,t4,"14316"), quote(lane3,t5,"40870"), quote(lane3,t6,"17808"),
                quote(lane3,t7,"20932"),

                quote(lane4,t1,"42481"), quote(lane4,t2,"45828"), quote(lane4,t3,"36447"),
                quote(lane4,t4,"10678"), quote(lane4,t5,"20635"), quote(lane4,t6,"36210"),
                quote(lane4,t7,"16897"),

                quote(lane5,t1,"19862"), quote(lane5,t2,"18297"), quote(lane5,t3,"12789"),
                quote(lane5,t4,"13032"), quote(lane5,t5,"26421"), quote(lane5,t6,"39444"),
                quote(lane5,t7,"27938")
            );
        }

        @Test
        @DisplayName("All 5 lanes are covered in the assignment")
        void allLanesCovered() {
            when(laneQuoteRepository.findAllWithLaneAndTransporter()).thenReturn(buildQuotes());

            AssignmentResponse resp = assignmentService.assign(req(3));

            assertThat(resp.getStatus()).isEqualTo("success");
            assertThat(resp.getAssignments()).hasSize(5);
            assertThat(resp.getAssignments())
                    .extracting(a -> a.getLaneId())
                    .containsExactlyInAnyOrder(1L, 2L, 3L, 4L, 5L);
        }

        @Test
        @DisplayName("At most 3 transporters are selected")
        void atMost3Transporters() {
            when(laneQuoteRepository.findAllWithLaneAndTransporter()).thenReturn(buildQuotes());

            AssignmentResponse resp = assignmentService.assign(req(3));

            assertThat(resp.getSelectedTransporters()).hasSizeLessThanOrEqualTo(3);
        }

        @Test
        @DisplayName("Total cost is minimised — unconstrained optimum is 59896 (4 transporters)")
        void costIsMinimised() {
            when(laneQuoteRepository.findAllWithLaneAndTransporter()).thenReturn(buildQuotes());

            AssignmentResponse resp = assignmentService.assign(req(3));

            // Unconstrained min = 11601+10512+14316+10678+12789 = 59896 (uses 4 transporters)
            // Best 3-transporter solution: drop T3, L5 goes T4=13032 → total=60139
            assertThat(resp.getTotalCost()).isEqualByComparingTo("60139");
        }

        @Test
        @DisplayName("selectedTransporters is a list of Long IDs")
        void selectedTransportersAreIds() {
            when(laneQuoteRepository.findAllWithLaneAndTransporter()).thenReturn(buildQuotes());

            AssignmentResponse resp = assignmentService.assign(req(3));

            // Verify they are transporter IDs (not names)
            assertThat(resp.getSelectedTransporters()).allMatch(id -> id > 0);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Phase 1 – Greedy unconstrained
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Phase 1 – Greedy unconstrained assignment")
    class Phase1Tests {

        @Test
        @DisplayName("Each lane is assigned to its cheapest transporter")
        void assignsCheapestPerLane() {
            List<LaneQuote> quotes = List.of(
                    quote(lane1, t1, "100"), quote(lane1, t2, "200"),
                    quote(lane2, t1, "300"), quote(lane2, t2, "150")
            );
            when(laneQuoteRepository.findAllWithLaneAndTransporter()).thenReturn(quotes);

            AssignmentResponse resp = assignmentService.assign(req(10));

            assertThat(resp.getStatus()).isEqualTo("success");
            assertThat(resp.getTotalCost()).isEqualByComparingTo("250");

            LaneAssignment l1 = findAssignment(resp, 1L);
            assertThat(l1.getTransporterId()).isEqualTo(1L);

            LaneAssignment l2 = findAssignment(resp, 2L);
            assertThat(l2.getTransporterId()).isEqualTo(2L);
        }

        @Test
        @DisplayName("Single lane, single transporter")
        void singleLaneSingleTransporter() {
            when(laneQuoteRepository.findAllWithLaneAndTransporter())
                    .thenReturn(List.of(quote(lane1, t1, "500")));

            AssignmentResponse resp = assignmentService.assign(req(1));

            assertThat(resp.getTotalCost()).isEqualByComparingTo("500");
            assertThat(resp.getSelectedTransporters()).containsExactly(1L);
            assertThat(resp.getAssignments()).hasSize(1);
        }

        @Test
        @DisplayName("All lanes are always covered regardless of transporter count")
        void allLanesCoveredUnconstrainedOptimal() {
            List<LaneQuote> quotes = List.of(
                    quote(lane1, t1, "50"),
                    quote(lane2, t2, "80"),
                    quote(lane3, t3, "120")
            );
            when(laneQuoteRepository.findAllWithLaneAndTransporter()).thenReturn(quotes);

            AssignmentResponse resp = assignmentService.assign(req(5));

            assertThat(resp.getAssignments())
                    .extracting(a -> a.getLaneId())
                    .containsExactlyInAnyOrder(1L, 2L, 3L);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Phase 2 – Consolidation (maxTransporters constraint)
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Phase 2 – Consolidation to satisfy maxTransporters")
    class Phase2Tests {

        @Test
        @DisplayName("Consolidates from 3 transporters to 2, choosing minimum-penalty drop")
        void consolidatesTo2() {
            /*
             * Phase 1: lane1→t1(100), lane2→t2(150), lane3→t3(200)  [3 transporters]
             * Drop penalties:
             *   drop t3: lane3→t1(220) → penalty=20
             *   drop t1: lane1→t2(180) → penalty=80
             *   drop t2: lane2→t1(300) → penalty=150
             * Best drop: t3. Final: t1(100)+t2(150)+t1(220)=470
             */
            List<LaneQuote> quotes = List.of(
                    quote(lane1,t1,"100"), quote(lane1,t2,"180"), quote(lane1,t3,"999"),
                    quote(lane2,t1,"300"), quote(lane2,t2,"150"), quote(lane2,t3,"999"),
                    quote(lane3,t1,"220"), quote(lane3,t2,"999"), quote(lane3,t3,"200")
            );
            when(laneQuoteRepository.findAllWithLaneAndTransporter()).thenReturn(quotes);

            AssignmentResponse resp = assignmentService.assign(req(2));

            assertThat(resp.getSelectedTransporters()).hasSize(2);
            assertThat(resp.getTotalCost()).isEqualByComparingTo("470");
            assertThat(resp.getSelectedTransporters()).containsExactlyInAnyOrder(1L, 2L);
        }

        @Test
        @DisplayName("maxTransporters=1 assigns all lanes to a single transporter")
        void allLanesToOneTransporter() {
            List<LaneQuote> quotes = List.of(
                    quote(lane1, t1, "100"), quote(lane1, t2, "200"),
                    quote(lane2, t1, "100"), quote(lane2, t2, "200"),
                    quote(lane3, t1, "100"), quote(lane3, t2, "200")
            );
            when(laneQuoteRepository.findAllWithLaneAndTransporter()).thenReturn(quotes);

            AssignmentResponse resp = assignmentService.assign(req(1));

            assertThat(resp.getSelectedTransporters()).hasSize(1);
            assertThat(resp.getTotalCost()).isEqualByComparingTo("300");
            assertThat(resp.getAssignments()).hasSize(3);  // all lanes still covered
        }

        @Test
        @DisplayName("No consolidation when unconstrained already within limit")
        void noConsolidationNeeded() {
            List<LaneQuote> quotes = List.of(
                    quote(lane1, t1, "50"), quote(lane1, t2, "200"),
                    quote(lane2, t1, "80"), quote(lane2, t2, "300")
            );
            when(laneQuoteRepository.findAllWithLaneAndTransporter()).thenReturn(quotes);

            AssignmentResponse resp = assignmentService.assign(req(3));

            assertThat(resp.getSelectedTransporters()).hasSize(1);
            assertThat(resp.getTotalCost()).isEqualByComparingTo("130");
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Response format validation
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Response format – matches PDF spec")
    class ResponseFormatTests {

        @Test
        @DisplayName("status field is lowercase 'success'")
        void statusIsLowercaseSuccess() {
            when(laneQuoteRepository.findAllWithLaneAndTransporter())
                    .thenReturn(List.of(quote(lane1, t1, "500")));

            AssignmentResponse resp = assignmentService.assign(req(1));

            assertThat(resp.getStatus()).isEqualTo("success");
        }

        @Test
        @DisplayName("selectedTransporters contains Long IDs, not names")
        void selectedTransportersAreIds() {
            when(laneQuoteRepository.findAllWithLaneAndTransporter())
                    .thenReturn(List.of(
                            quote(lane1, t4, "100"),
                            quote(lane2, t6, "200")
                    ));

            AssignmentResponse resp = assignmentService.assign(req(5));

            // Should contain IDs 4 and 6 (not names)
            assertThat(resp.getSelectedTransporters()).containsExactlyInAnyOrder(4L, 6L);
        }

        @Test
        @DisplayName("Assignments are sorted by laneId ascending")
        void assignmentsSortedByLaneId() {
            when(laneQuoteRepository.findAllWithLaneAndTransporter())
                    .thenReturn(List.of(
                            quote(lane3, t1, "100"),
                            quote(lane1, t1, "200"),
                            quote(lane2, t1, "150")
                    ));

            AssignmentResponse resp = assignmentService.assign(req(5));

            List<Long> laneIds = resp.getAssignments().stream()
                    .map(LaneAssignment::getLaneId)
                    .toList();
            assertThat(laneIds).isSortedAccordingTo(Long::compareTo);
        }

        @Test
        @DisplayName("totalCost equals sum of all assigned lane costs")
        void totalCostIsCorrect() {
            when(laneQuoteRepository.findAllWithLaneAndTransporter())
                    .thenReturn(List.of(
                            quote(lane1, t1, "111.11"),
                            quote(lane2, t1, "222.22"),
                            quote(lane3, t1, "333.33")
                    ));

            AssignmentResponse resp = assignmentService.assign(req(1));

            assertThat(resp.getTotalCost()).isEqualByComparingTo("666.66");
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Error cases
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Error cases")
    class ErrorTests {

        @Test
        @DisplayName("Throws InsufficientDataException when no quotes exist")
        void throwsWhenNoQuotes() {
            when(laneQuoteRepository.findAllWithLaneAndTransporter())
                    .thenReturn(Collections.emptyList());

            assertThatThrownBy(() -> assignmentService.assign(req(3)))
                    .isInstanceOf(InsufficientDataException.class)
                    .hasMessageContaining("No lane quotes found");
        }

        @Test
        @DisplayName("Throws InsufficientDataException when constraint is infeasible")
        void throwsWhenConstraintInfeasible() {
            // lane1 only quoted by t1, lane2 only quoted by t2 → maxTransporters=1 impossible
            List<LaneQuote> quotes = List.of(
                    quote(lane1, t1, "100"),
                    quote(lane2, t2, "200")
            );
            when(laneQuoteRepository.findAllWithLaneAndTransporter()).thenReturn(quotes);

            assertThatThrownBy(() -> assignmentService.assign(req(1)))
                    .isInstanceOf(InsufficientDataException.class);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // consolidate() white-box tests
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("consolidate() helper – white-box")
    class ConsolidateTests {

        @Test
        @DisplayName("Reduces to exactly maxTransporters")
        void reducesCorrectly() {
            Map<Long, LaneQuote> initial = new HashMap<>();
            initial.put(1L, quote(lane1, t1, "100"));
            initial.put(2L, quote(lane2, t2, "150"));
            initial.put(3L, quote(lane3, t3, "200"));

            Map<Long, List<LaneQuote>> byLane = new HashMap<>();
            byLane.put(1L, List.of(quote(lane1, t1, "100"), quote(lane1, t2, "180")));
            byLane.put(2L, List.of(quote(lane2, t2, "150"), quote(lane2, t1, "300")));
            byLane.put(3L, List.of(quote(lane3, t3, "200"), quote(lane3, t1, "220")));

            Map<Long, LaneQuote> result =
                    assignmentService.consolidate(new HashMap<>(initial), byLane, 2);

            long distinct = result.values().stream()
                    .map(lq -> lq.getTransporter().getId()).distinct().count();
            assertThat(distinct).isEqualTo(2);
        }

        @Test
        @DisplayName("Returns unchanged when already within limit")
        void noChangeWhenWithinLimit() {
            Map<Long, LaneQuote> initial = new HashMap<>();
            initial.put(1L, quote(lane1, t1, "100"));
            initial.put(2L, quote(lane2, t1, "150"));

            Map<Long, List<LaneQuote>> byLane = new HashMap<>();
            byLane.put(1L, List.of(quote(lane1, t1, "100")));
            byLane.put(2L, List.of(quote(lane2, t1, "150")));

            Map<Long, LaneQuote> result =
                    assignmentService.consolidate(new HashMap<>(initial), byLane, 3);

            assertThat(result).hasSize(2);
        }
    }

    // ── Utility ───────────────────────────────────────────────────────────────

    private LaneAssignment findAssignment(AssignmentResponse resp, Long laneId) {
        return resp.getAssignments().stream()
                .filter(a -> a.getLaneId().equals(laneId))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Lane " + laneId + " not found"));
    }
}
