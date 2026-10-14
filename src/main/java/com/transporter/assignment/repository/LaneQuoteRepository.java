package com.transporter.assignment.repository;

import com.transporter.assignment.entity.LaneQuote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LaneQuoteRepository extends JpaRepository<LaneQuote, Long> {

    /**
     * Eagerly loads lane and transporter to avoid N+1 in the assignment algorithm.
     */
    @Query("SELECT lq FROM LaneQuote lq JOIN FETCH lq.lane JOIN FETCH lq.transporter")
    List<LaneQuote> findAllWithLaneAndTransporter();
}
