package com.adnan.guardianai.service;

import com.adnan.guardianai.model.FileScanResult;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;

@Service
public class FileScannerService {

    private static final int MAX_SCAN_SIZE = 5 * 1024 * 1024;

    public FileScanResult scan(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Upload a non-empty file.");
        }
        if (file.getSize() > MAX_SCAN_SIZE) {
            throw new IllegalArgumentException("File is too large for the safe demo scanner. Maximum size is 5 MB.");
        }

        byte[] bytes = file.getBytes();
        List<String> findings = new ArrayList<>();
        int riskScore = 0;

        String fileName = safeFileName(file.getOriginalFilename());
        String lowerName = fileName.toLowerCase(Locale.ROOT);
        String textView = new String(bytes, StandardCharsets.ISO_8859_1);
        String lowerText = textView.toLowerCase(Locale.ROOT);

        if (textView.contains("X5O!P%@AP[4\\PZX54(P^)7CC)7}$EICAR")) {
            riskScore += 95;
            findings.add("Known antivirus test signature detected. This is a harmless test string used to validate scanners.");
        }
        if (lowerText.contains("powershell") && lowerText.contains("-encodedcommand")) {
            riskScore += 35;
            findings.add("Suspicious PowerShell encoded command indicator found.");
        }
        if (lowerText.contains("wscript.shell") || lowerText.contains("createobject(")) {
            riskScore += 25;
            findings.add("Script automation behavior indicator found.");
        }
        if (lowerText.contains("autoopen") || lowerText.contains("document_open")) {
            riskScore += 25;
            findings.add("Office macro auto-run indicator found.");
        }
        if (lowerText.contains("cmd.exe") || lowerText.contains("rundll32") || lowerText.contains("regsvr32")) {
            riskScore += 20;
            findings.add("Command execution or living-off-the-land binary reference found.");
        }
        if (bytes.length >= 2 && bytes[0] == 'M' && bytes[1] == 'Z') {
            riskScore += 20;
            findings.add("Windows executable header detected.");
        }
        if (lowerName.endsWith(".exe") || lowerName.endsWith(".scr") || lowerName.endsWith(".bat") || lowerName.endsWith(".ps1") || lowerName.endsWith(".vbs")) {
            riskScore += 15;
            findings.add("File extension is commonly used for executable or script content.");
        }

        if (findings.isEmpty()) {
            findings.add("No suspicious indicators were detected by the static scanner.");
        }

        FileScanResult result = new FileScanResult();
        result.setFileName(fileName);
        result.setFileSize(file.getSize());
        result.setSha256(sha256(bytes));
        result.setRiskScore(Math.min(riskScore, 100));
        result.setSeverity(severityFor(result.getRiskScore()));
        result.setVerdict(verdictFor(result.getRiskScore()));
        result.setFindings(String.join("\n", findings));
        return result;
    }

    private String safeFileName(String originalName) {
        if (originalName == null || originalName.isBlank()) {
            return "uploaded-file";
        }
        return originalName.replaceAll("[\\\\/]", "_");
    }

    private String sha256(byte[] bytes) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(bytes));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is not available.", ex);
        }
    }

    private String severityFor(int riskScore) {
        if (riskScore >= 80) {
            return "Critical";
        }
        if (riskScore >= 55) {
            return "High";
        }
        if (riskScore >= 25) {
            return "Medium";
        }
        return "Low";
    }

    private String verdictFor(int riskScore) {
        if (riskScore >= 80) {
            return "Malicious or Test Malware Signature";
        }
        if (riskScore >= 55) {
            return "Likely Suspicious";
        }
        if (riskScore >= 25) {
            return "Needs Analyst Review";
        }
        return "No Threat Detected";
    }
}
