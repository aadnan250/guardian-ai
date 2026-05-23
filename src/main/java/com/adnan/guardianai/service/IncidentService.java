package com.adnan.guardianai.service;

import com.adnan.guardianai.model.Incident;
import com.adnan.guardianai.model.LogUpload;
import com.adnan.guardianai.repository.IncidentRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class IncidentService {

    private final IncidentRepository incidentRepository;
    private final AiAnalysisService aiAnalysisService;

    public IncidentService(IncidentRepository incidentRepository, AiAnalysisService aiAnalysisService) {
        this.incidentRepository = incidentRepository;
        this.aiAnalysisService = aiAnalysisService;
    }

    public List<Incident> saveDetectedIncidents(List<Incident> incidents, LogUpload logUpload) {
        incidents.forEach(incident -> {
            incident.setLogUpload(logUpload);
            incident.setAiExplanation(aiAnalysisService.analyzeIncident(incident));
        });
        return incidentRepository.saveAll(incidents);
    }

    public List<Incident> findAll() {
        return incidentRepository.findAll();
    }

    public List<Incident> findRecent() {
        return incidentRepository.findTop10ByOrderByDetectedAtDesc();
    }

    public Incident findById(Long id) {
        return incidentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Incident not found: " + id));
    }

    public List<Incident> findBySeverity(String severity) {
        return incidentRepository.findBySeverityIgnoreCase(severity);
    }

    public long countBySeverity(String severity) {
        return incidentRepository.countBySeverityIgnoreCase(severity);
    }
}
