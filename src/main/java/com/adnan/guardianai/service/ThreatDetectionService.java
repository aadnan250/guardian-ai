package com.adnan.guardianai.service;

import com.adnan.guardianai.model.Incident;
import com.adnan.guardianai.model.LogEntry;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ThreatDetectionService {

    private static final int FAILED_LOGIN_THRESHOLD = 5;
    private static final Set<String> PRIVILEGED_USERS = Set.of("admin", "root", "administrator");

    public List<Incident> detectThreats(List<LogEntry> entries) {
        Map<String, List<LogEntry>> byIp = entries.stream()
                .filter(entry -> entry.getIpAddress() != null)
                .collect(Collectors.groupingBy(LogEntry::getIpAddress));

        return byIp.entrySet().stream()
                .flatMap(entry -> detectForIp(entry.getKey(), entry.getValue()).stream())
                .toList();
    }

    private List<Incident> detectForIp(String ipAddress, List<LogEntry> entries) {
        List<LogEntry> sortedEntries = entries.stream()
                .sorted(Comparator.comparing(LogEntry::getTimestamp))
                .toList();

        long failedLogins = sortedEntries.stream()
                .filter(entry -> "FAILED_LOGIN".equals(entry.getEventType()))
                .count();
        boolean successAfterFailures = hasSuccessAfterFailures(sortedEntries);
        Set<String> usernames = sortedEntries.stream()
                .map(LogEntry::getUsername)
                .collect(Collectors.toCollection(HashSet::new));
        boolean privilegedTargeted = usernames.stream().anyMatch(this::isPrivilegedUser);
        boolean unusualLoginTime = sortedEntries.stream().anyMatch(this::isUnusualLoginTime);
        String primaryUser = usernames.stream().sorted().findFirst().orElse("unknown");
        LocalDateTime detectedAt = sortedEntries.get(sortedEntries.size() - 1).getTimestamp();

        List<Incident> incidents = new java.util.ArrayList<>();

        if (failedLogins >= FAILED_LOGIN_THRESHOLD) {
            incidents.add(buildIncident(
                    successAfterFailures ? "Possible Account Compromise" : "Brute Force Login Attempt",
                    successAfterFailures ? "Critical" : severityWithPrivilege("High", privilegedTargeted),
                    ipAddress,
                    primaryUser,
                    confidenceScore((int) failedLogins, successAfterFailures, privilegedTargeted),
                    detectedAt
            ));
        }

        if (privilegedTargeted && failedLogins > 0) {
            incidents.add(buildIncident(
                    "Privileged Account Targeted",
                    "High",
                    ipAddress,
                    primaryUser,
                    Math.min(95, 75 + (int) failedLogins),
                    detectedAt
            ));
        }

        if (usernames.size() >= 4 && failedLogins >= 4) {
            incidents.add(buildIncident(
                    "Credential Stuffing",
                    "High",
                    ipAddress,
                    String.join(", ", usernames),
                    88,
                    detectedAt
            ));
        }

        if (unusualLoginTime) {
            incidents.add(buildIncident(
                    "Unusual Login Time",
                    "Medium",
                    ipAddress,
                    primaryUser,
                    65,
                    detectedAt
            ));
        }

        return incidents;
    }

    private Incident buildIncident(String threatType, String severity, String ipAddress, String username, int confidenceScore, LocalDateTime detectedAt) {
        Incident incident = new Incident();
        incident.setThreatType(threatType);
        incident.setSeverity(severity);
        incident.setIpAddress(ipAddress);
        incident.setUsername(username);
        incident.setConfidenceScore(confidenceScore);
        incident.setDetectedAt(detectedAt);
        return incident;
    }

    private boolean hasSuccessAfterFailures(List<LogEntry> sortedEntries) {
        int failedCount = 0;
        for (LogEntry entry : sortedEntries) {
            if ("FAILED_LOGIN".equals(entry.getEventType())) {
                failedCount++;
            }
            if ("SUCCESS_LOGIN".equals(entry.getEventType()) && failedCount >= FAILED_LOGIN_THRESHOLD) {
                return true;
            }
        }
        return false;
    }

    private int confidenceScore(int failedLogins, boolean successAfterFailures, boolean privilegedTargeted) {
        int score;
        if (failedLogins >= 10) {
            score = 95;
        } else if (failedLogins >= 8) {
            score = 85;
        } else {
            score = 70;
        }
        if (successAfterFailures) {
            score = 95;
        }
        if (privilegedTargeted) {
            score += 10;
        }
        return Math.min(score, 95);
    }

    private String severityWithPrivilege(String baseSeverity, boolean privilegedTargeted) {
        return privilegedTargeted ? "High" : baseSeverity;
    }

    private boolean isPrivilegedUser(String username) {
        return username != null && PRIVILEGED_USERS.contains(username.toLowerCase());
    }

    private boolean isUnusualLoginTime(LogEntry entry) {
        if (!"SUCCESS_LOGIN".equals(entry.getEventType()) || entry.getTimestamp() == null) {
            return false;
        }
        int hour = entry.getTimestamp().getHour();
        return hour >= 2 && hour < 5;
    }
}
