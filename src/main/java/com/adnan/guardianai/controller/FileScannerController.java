package com.adnan.guardianai.controller;

import com.adnan.guardianai.repository.FileScanResultRepository;
import com.adnan.guardianai.service.FileScannerService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Controller
public class FileScannerController {

    private final FileScannerService fileScannerService;
    private final FileScanResultRepository fileScanResultRepository;

    public FileScannerController(FileScannerService fileScannerService, FileScanResultRepository fileScanResultRepository) {
        this.fileScannerService = fileScannerService;
        this.fileScanResultRepository = fileScanResultRepository;
    }

    @GetMapping("/scanner")
    public String scanner(Model model) {
        model.addAttribute("recentScans", fileScanResultRepository.findTop10ByOrderByScannedAtDesc());
        return "scanner";
    }

    @PostMapping("/scanner")
    public String scan(@RequestParam("file") MultipartFile file, Model model) throws IOException {
        var result = fileScannerService.scan(file);
        var savedResult = fileScanResultRepository.save(result);
        model.addAttribute("scanResult", savedResult);
        model.addAttribute("recentScans", fileScanResultRepository.findTop10ByOrderByScannedAtDesc());
        return "scanner";
    }
}
