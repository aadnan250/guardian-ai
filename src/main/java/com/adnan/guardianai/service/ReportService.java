package com.adnan.guardianai.service;

import com.adnan.guardianai.model.Incident;
import org.springframework.stereotype.Service;

@Service
public class ReportService {

    public String generateTextReport(Incident incident) {
        return """
                GuardianAI Incident Report

                Incident ID: %d
                Threat Type: %s
                Severity: %s
                Confidence Score: %d%%
                Affected Username: %s
                Suspicious IP: %s
                Detected Time: %s

                AI Analysis:
                %s
                """.formatted(
                incident.getId(),
                incident.getThreatType(),
                incident.getSeverity(),
                incident.getConfidenceScore(),
                incident.getUsername(),
                incident.getIpAddress(),
                incident.getDetectedAt(),
                incident.getAiExplanation()
        );
    }
}
