package com.adnan.guardianai.dto;

import com.adnan.guardianai.model.Incident;

import java.time.LocalDateTime;

public record IncidentDTO(
        Long id,
        String threatType,
        String severity,
        String ipAddress,
        String username,
        int confidenceScore,
        String aiExplanation,
        LocalDateTime detectedAt
) {
    public static IncidentDTO from(Incident incident) {
        return new IncidentDTO(
                incident.getId(),
                incident.getThreatType(),
                incident.getSeverity(),
                incident.getIpAddress(),
                incident.getUsername(),
                incident.getConfidenceScore(),
                incident.getAiExplanation(),
                incident.getDetectedAt()
        );
    }
}
