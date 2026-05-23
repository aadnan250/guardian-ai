package com.adnan.guardianai.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LogParserServiceTests {

    private final LogParserService logParserService = new LogParserService();

    @Test
    void parsesAuthenticationLogLine() {
        var entry = logParserService.parseLine("2026-05-23 10:15:22 FAILED_LOGIN user=admin ip=192.168.1.50");

        assertThat(entry).isPresent();
        assertThat(entry.get().getEventType()).isEqualTo("FAILED_LOGIN");
        assertThat(entry.get().getUsername()).isEqualTo("admin");
        assertThat(entry.get().getIpAddress()).isEqualTo("192.168.1.50");
    }
}
