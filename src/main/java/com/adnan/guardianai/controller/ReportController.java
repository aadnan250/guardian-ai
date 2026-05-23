package com.adnan.guardianai.controller;

import com.adnan.guardianai.service.IncidentService;
import com.adnan.guardianai.service.ReportService;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ReportController {

    private final IncidentService incidentService;
    private final ReportService reportService;

    public ReportController(IncidentService incidentService, ReportService reportService) {
        this.incidentService = incidentService;
        this.reportService = reportService;
    }

    @GetMapping("/api/reports/{incidentId}")
    public ResponseEntity<String> report(@PathVariable Long incidentId) {
        String report = reportService.generateTextReport(incidentService.findById(incidentId));
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_PLAIN)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename("guardianai-incident-" + incidentId + ".txt")
                        .build()
                        .toString())
                .body(report);
    }
}
