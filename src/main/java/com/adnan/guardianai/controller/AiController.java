package com.adnan.guardianai.controller;

import com.adnan.guardianai.service.AiAnalysisService;
import com.adnan.guardianai.service.IncidentService;
import com.adnan.guardianai.model.Incident;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class AiController {

    private final IncidentService incidentService;
    private final AiAnalysisService aiAnalysisService;

    public AiController(IncidentService incidentService, AiAnalysisService aiAnalysisService) {
        this.incidentService = incidentService;
        this.aiAnalysisService = aiAnalysisService;
    }

    @PostMapping("/api/ai/analyze/{incidentId}")
    public ResponseEntity<?> analyze(@PathVariable Long incidentId) {
        return ResponseEntity.ok(Map.of("analysis", aiAnalysisService.analyzeIncident(incidentService.findById(incidentId))));
    }

    @PostMapping("/api/ai/analyze")
    public ResponseEntity<?> analyzeDraft(@RequestBody Incident incident) {
        return ResponseEntity.ok(Map.of("analysis", aiAnalysisService.analyzeIncident(incident)));
    }
}
