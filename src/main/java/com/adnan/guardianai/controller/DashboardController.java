package com.adnan.guardianai.controller;

import com.adnan.guardianai.repository.IncidentRepository;
import com.adnan.guardianai.repository.LogUploadRepository;
import com.adnan.guardianai.service.IncidentService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    private final IncidentRepository incidentRepository;
    private final LogUploadRepository logUploadRepository;
    private final IncidentService incidentService;

    public DashboardController(IncidentRepository incidentRepository, LogUploadRepository logUploadRepository, IncidentService incidentService) {
        this.incidentRepository = incidentRepository;
        this.logUploadRepository = logUploadRepository;
        this.incidentService = incidentService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("totalIncidents", incidentRepository.count());
        model.addAttribute("criticalIncidents", incidentService.countBySeverity("Critical"));
        model.addAttribute("highIncidents", incidentService.countBySeverity("High"));
        model.addAttribute("mediumIncidents", incidentService.countBySeverity("Medium"));
        model.addAttribute("lowIncidents", incidentService.countBySeverity("Low"));
        model.addAttribute("recentUploads", logUploadRepository.findTop5ByOrderByUploadTimeDesc());
        model.addAttribute("recentIncidents", incidentService.findRecent());
        return "dashboard";
    }
}
