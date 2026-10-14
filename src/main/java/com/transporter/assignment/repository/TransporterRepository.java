package com.transporter.assignment.repository;

import com.transporter.assignment.entity.Transporter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TransporterRepository extends JpaRepository<Transporter, Long> {

    Optional<Transporter> findByName(String name);
}
