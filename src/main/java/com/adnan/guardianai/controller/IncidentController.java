package com.adnan.guardianai.controller;

import com.adnan.guardianai.dto.IncidentDTO;
import com.adnan.guardianai.model.Incident;
import com.adnan.guardianai.service.IncidentService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class IncidentController {

    private final IncidentService incidentService;

    public IncidentController(IncidentService incidentService) {
        this.incidentService = incidentService;
    }

    @GetMapping("/incidents/{id}")
    public String incidentDetails(@PathVariable Long id, Model model) {
        model.addAttribute("incident", incidentService.findById(id));
        return "incident-details";
    }

    @GetMapping("/api/incidents")
    @ResponseBody
    public List<IncidentDTO> incidents() {
        return incidentService.findAll().stream().map(IncidentDTO::from).toList();
    }

    @GetMapping("/api/incidents/{id}")
    @ResponseBody
    public IncidentDTO incidentById(@PathVariable Long id) {
        return IncidentDTO.from(incidentService.findById(id));
    }

    @GetMapping("/api/incidents/severity/{severity}")
    @ResponseBody
    public List<IncidentDTO> incidentsBySeverity(@PathVariable String severity) {
        return incidentService.findBySeverity(severity).stream().map(IncidentDTO::from).toList();
    }
}
