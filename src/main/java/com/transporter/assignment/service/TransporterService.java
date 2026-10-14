package com.transporter.assignment.service;

import com.transporter.assignment.dto.InputRequest;
import com.transporter.assignment.entity.Lane;
import com.transporter.assignment.entity.LaneQuote;
import com.transporter.assignment.entity.Transporter;
import com.transporter.assignment.repository.LaneQuoteRepository;
import com.transporter.assignment.repository.LaneRepository;
import com.transporter.assignment.repository.TransporterRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

/**
 * Persists the input payload (lanes, transporters and nested lane quotes).
 * Each call replaces all existing data in a single transaction.
 */
@Service
public class TransporterService {

    private static final Logger log = LoggerFactory.getLogger(TransporterService.class);

    private final LaneRepository laneRepository;
    private final TransporterRepository transporterRepository;
    private final LaneQuoteRepository laneQuoteRepository;

    public TransporterService(LaneRepository laneRepository,
                              TransporterRepository transporterRepository,
                              LaneQuoteRepository laneQuoteRepository) {
        this.laneRepository = laneRepository;
        this.transporterRepository = transporterRepository;
        this.laneQuoteRepository = laneQuoteRepository;
    }

    /**
     * Clears all existing data and saves the new input.
     *
     * @param request payload with lanes and transporters (each carrying their laneQuotes)
     * @return human-readable summary
     */
    @Transactional
    public String saveInput(InputRequest request) {
        log.info("Saving input: {} lanes, {} transporters",
                request.getLanes().size(), request.getTransporters().size());

        // ── Clear in FK-safe order ─────────────────────────────────
        laneQuoteRepository.deleteAllInBatch();
        laneRepository.deleteAllInBatch();
        transporterRepository.deleteAllInBatch();

        // ── Persist Lanes ──────────────────────────────────────────
        // Build a lookup map: clientId → Lane entity
        Map<Long, Lane> laneMap = new HashMap<>();
        for (InputRequest.LaneDto dto : request.getLanes()) {
            if (dto.getId() == null) {
                throw new IllegalArgumentException("Every lane must have an 'id' field.");
            }
            Lane lane = Lane.builder()
                    .id(dto.getId())
                    .origin(dto.getOrigin())
                    .destination(dto.getDestination())
                    .build();
            laneMap.put(dto.getId(), laneRepository.save(lane));
        }
        log.debug("Saved {} lanes", laneMap.size());

        // ── Persist Transporters + nested LaneQuotes ───────────────
        int totalQuotes = 0;
        for (InputRequest.TransporterDto tDto : request.getTransporters()) {
            if (tDto.getId() == null) {
                throw new IllegalArgumentException("Every transporter must have an 'id' field.");
            }

            Transporter transporter = Transporter.builder()
                    .id(tDto.getId())
                    .name(tDto.getName())
                    .build();
            transporterRepository.save(transporter);

            // Persist each lane quote nested inside this transporter
            for (InputRequest.LaneQuoteDto qDto : tDto.getLaneQuotes()) {
                Lane lane = laneMap.get(qDto.getLaneId());
                if (lane == null) {
                    throw new IllegalArgumentException(
                            "Transporter id=" + tDto.getId() +
                            " references unknown laneId=" + qDto.getLaneId());
                }
                if (qDto.getQuote() == null || qDto.getQuote().signum() < 0) {
                    throw new IllegalArgumentException(
                            "Quote must be non-negative for laneId=" + qDto.getLaneId() +
                            " from transporter id=" + tDto.getId());
                }

                LaneQuote quote = LaneQuote.builder()
                        .lane(lane)
                        .transporter(transporter)
                        .cost(qDto.getQuote())       // spec calls it "quote", stored as cost
                        .build();
                laneQuoteRepository.save(quote);
                totalQuotes++;
            }
        }

        log.info("Input saved: {} lanes, {} transporters, {} quotes",
                laneMap.size(), request.getTransporters().size(), totalQuotes);

        return String.format("Input data saved successfully.");
    }
}
