package com.adnan.guardianai.service;

import com.adnan.guardianai.model.LogEntry;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class LogParserService {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final Pattern EVENT_PATTERN = Pattern.compile("\\b(FAILED_LOGIN|SUCCESS_LOGIN)\\b");
    private static final Pattern USER_PATTERN = Pattern.compile("\\buser=([^\\s]+)");
    private static final Pattern IP_PATTERN = Pattern.compile("\\bip=([^\\s]+)");

    public List<LogEntry> parse(MultipartFile file) throws IOException {
        List<LogEntry> entries = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                parseLine(line).ifPresent(entries::add);
            }
        }

        return entries;
    }

    Optional<LogEntry> parseLine(String line) {
        if (line == null || line.isBlank()) {
            return Optional.empty();
        }

        Matcher eventMatcher = EVENT_PATTERN.matcher(line);
        Matcher userMatcher = USER_PATTERN.matcher(line);
        Matcher ipMatcher = IP_PATTERN.matcher(line);

        if (!eventMatcher.find() || !userMatcher.find() || !ipMatcher.find() || line.length() < 19) {
            return Optional.empty();
        }

        LogEntry entry = new LogEntry();
        entry.setTimestamp(LocalDateTime.parse(line.substring(0, 19), FORMATTER));
        entry.setEventType(eventMatcher.group(1));
        entry.setUsername(userMatcher.group(1));
        entry.setIpAddress(ipMatcher.group(1));
        entry.setRawLine(line);
        return Optional.of(entry);
    }
}
