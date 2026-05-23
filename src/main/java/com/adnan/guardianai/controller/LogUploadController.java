package com.adnan.guardianai.controller;

import com.adnan.guardianai.model.Incident;
import com.adnan.guardianai.model.LogEntry;
import com.adnan.guardianai.model.LogUpload;
import com.adnan.guardianai.model.UploadStatus;
import com.adnan.guardianai.model.User;
import com.adnan.guardianai.repository.LogUploadRepository;
import com.adnan.guardianai.repository.UserRepository;
import com.adnan.guardianai.service.IncidentService;
import com.adnan.guardianai.service.LogParserService;
import com.adnan.guardianai.service.ThreatDetectionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Controller
public class LogUploadController {

    private final LogParserService logParserService;
    private final ThreatDetectionService threatDetectionService;
    private final IncidentService incidentService;
    private final LogUploadRepository logUploadRepository;
    private final UserRepository userRepository;

    public LogUploadController(
            LogParserService logParserService,
            ThreatDetectionService threatDetectionService,
            IncidentService incidentService,
            LogUploadRepository logUploadRepository,
            UserRepository userRepository
    ) {
        this.logParserService = logParserService;
        this.threatDetectionService = threatDetectionService;
        this.incidentService = incidentService;
        this.logUploadRepository = logUploadRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/logs/upload")
    public String uploadPage() {
        return "upload";
    }

    @PostMapping("/logs/upload")
    public String upload(@RequestParam("file") MultipartFile file, Authentication authentication, Model model) throws IOException {
        List<Incident> savedIncidents = processUpload(file, authentication);
        model.addAttribute("message", "Analyzed " + file.getOriginalFilename() + " and detected " + savedIncidents.size() + " incident(s).");
        return "upload";
    }

    @PostMapping("/api/logs/upload")
    @ResponseBody
    public ResponseEntity<?> apiUpload(@RequestParam("file") MultipartFile file, Authentication authentication) throws IOException {
        return ResponseEntity.ok(processUpload(file, authentication));
    }

    @GetMapping("/api/logs")
    @ResponseBody
    public List<LogUpload> logs() {
        return logUploadRepository.findAll();
    }

    @GetMapping("/api/logs/{id}")
    @ResponseBody
    public LogUpload logById(@PathVariable Long id) {
        return logUploadRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Log upload not found: " + id));
    }

    private List<Incident> processUpload(MultipartFile file, Authentication authentication) throws IOException {
        validateLogFile(file);

        LogUpload upload = new LogUpload();
        upload.setFileName(file.getOriginalFilename());
        upload.setStatus(UploadStatus.PROCESSED);
        upload.setUploadedBy(currentUser(authentication));
        LogUpload savedUpload = logUploadRepository.save(upload);

        List<LogEntry> entries = logParserService.parse(file);
        List<Incident> incidents = threatDetectionService.detectThreats(entries);
        return incidentService.saveDetectedIncidents(incidents, savedUpload);
    }

    private void validateLogFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Upload a non-empty log file.");
        }
        String fileName = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase();
        if (!fileName.endsWith(".txt") && !fileName.endsWith(".log")) {
            throw new IllegalArgumentException("Only .txt and .log files are supported.");
        }
    }

    private User currentUser(Authentication authentication) {
        if (authentication == null) {
            return null;
        }
        return userRepository.findByEmail(authentication.getName()).orElse(null);
    }
}
