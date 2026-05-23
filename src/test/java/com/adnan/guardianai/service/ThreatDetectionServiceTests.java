package com.adnan.guardianai.service;

import com.adnan.guardianai.model.LogEntry;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

class ThreatDetectionServiceTests {

    private final ThreatDetectionService threatDetectionService = new ThreatDetectionService();

    @Test
    void detectsPossibleCompromiseAfterFailedLoginsAndSuccess() {
        var entries = new ArrayList<LogEntry>();
        for (int i = 0; i < 5; i++) {
            entries.add(entry("FAILED_LOGIN", "admin", "192.168.1.50", LocalDateTime.of(2026, 5, 23, 10, 15, i)));
        }
        entries.add(entry("SUCCESS_LOGIN", "admin", "192.168.1.50", LocalDateTime.of(2026, 5, 23, 10, 17)));

        var incidents = threatDetectionService.detectThreats(entries);

        assertThat(incidents)
                .anySatisfy(incident -> {
                    assertThat(incident.getThreatType()).isEqualTo("Possible Account Compromise");
                    assertThat(incident.getSeverity()).isEqualTo("Critical");
                    assertThat(incident.getConfidenceScore()).isEqualTo(95);
                });
    }

    private LogEntry entry(String eventType, String username, String ipAddress, LocalDateTime timestamp) {
        LogEntry entry = new LogEntry();
        entry.setEventType(eventType);
        entry.setUsername(username);
        entry.setIpAddress(ipAddress);
        entry.setTimestamp(timestamp);
        return entry;
    }
}
