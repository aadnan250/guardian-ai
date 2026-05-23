package com.adnan.guardianai.repository;

import com.adnan.guardianai.model.Incident;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IncidentRepository extends JpaRepository<Incident, Long> {
    List<Incident> findBySeverityIgnoreCase(String severity);
    long countBySeverityIgnoreCase(String severity);
    List<Incident> findTop10ByOrderByDetectedAtDesc();
}
