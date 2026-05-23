package com.adnan.guardianai.service;

import com.adnan.guardianai.model.Incident;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
public class AiAnalysisService {

    private final RestClient restClient;
    private final String provider;
    private final String apiKey;
    private final String model;
    private final String endpoint;

    public AiAnalysisService(
            RestClient.Builder restClientBuilder,
            @Value("${guardianai.ai.provider}") String provider,
            @Value("${guardianai.ai.remote.api-key}") String apiKey,
            @Value("${guardianai.ai.remote.model}") String model,
            @Value("${guardianai.ai.remote.endpoint}") String endpoint
    ) {
        this.restClient = restClientBuilder.build();
        this.provider = provider;
        this.apiKey = apiKey;
        this.model = model;
        this.endpoint = endpoint;
    }

    public String analyzeIncident(Incident incident) {
        if ("remote".equalsIgnoreCase(provider) && apiKey != null && !apiKey.isBlank()) {
            return analyzeWithRemoteProvider(incident);
        }
        return localDefensiveAnalysis(incident);
    }

    private String analyzeWithRemoteProvider(Incident incident) {
        String prompt = buildPrompt(incident);
        Map<String, Object> requestBody = Map.of(
                "model", model,
                "messages", List.of(
                        Map.of("role", "system", "content", "You are a defensive cybersecurity SOC analyst. Provide only safe, defensive incident response guidance."),
                        Map.of("role", "user", "content", prompt)
                )
        );

        try {
            Map<?, ?> response = restClient.post()
                    .uri(endpoint)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(Map.class);

            List<?> choices = (List<?>) response.get("choices");
            Map<?, ?> firstChoice = (Map<?, ?>) choices.get(0);
            Map<?, ?> message = (Map<?, ?>) firstChoice.get("message");
            return String.valueOf(message.get("content"));
        } catch (RuntimeException ex) {
            return localDefensiveAnalysis(incident) + "\n\nAI provider fallback: Remote analysis failed, so GuardianAI generated a local defensive explanation.";
        }
    }

    private String buildPrompt(Incident incident) {
        return """
                Analyze this detected incident for a defensive cybersecurity incident response report.

                Threat Type: %s
                IP Address: %s
                Username: %s
                Confidence Score: %d%%
                Severity: %s

                Explain what happened, why it is suspicious, a likely MITRE ATT&CK-style category,
                severity justification, recommended response steps, and prevention recommendations.
                Do not provide offensive instructions.
                """.formatted(
                incident.getThreatType(),
                incident.getIpAddress(),
                incident.getUsername(),
                incident.getConfidenceScore(),
                incident.getSeverity()
        );
    }

    private String localDefensiveAnalysis(Incident incident) {
        String mitreCategory = switch (incident.getThreatType()) {
            case "Possible Account Compromise" -> "Valid Accounts / Credential Access";
            case "Brute Force Login Attempt" -> "Credential Access: Brute Force";
            case "Credential Stuffing" -> "Credential Access: Automated Login Attempts";
            case "Privileged Account Targeted" -> "Privilege Escalation / Valid Accounts";
            case "Unusual Login Time" -> "Initial Access / Anomalous Authentication";
            default -> "Suspicious Authentication Activity";
        };

        return """
                Summary:
                GuardianAI detected %s involving username "%s" from IP address %s.

                Why this is suspicious:
                The activity pattern matches behavior commonly reviewed by SOC analysts during authentication log triage. The confidence score is %d%% and the assigned severity is %s.

                MITRE ATT&CK-style mapping:
                %s

                Recommended response steps:
                - Review authentication logs for the affected account and source IP.
                - Reset the affected account password if compromise is suspected.
                - Block or rate-limit the suspicious source IP where appropriate.
                - Enable or verify multi-factor authentication.
                - Check for unusual account activity after the detected event.

                Prevention recommendations:
                - Enforce MFA for privileged accounts.
                - Use account lockout or adaptive throttling after repeated failed logins.
                - Monitor privileged account authentication separately.
                - Alert on successful logins following repeated failures.
                """.formatted(
                incident.getThreatType(),
                incident.getUsername(),
                incident.getIpAddress(),
                incident.getConfidenceScore(),
                incident.getSeverity(),
                mitreCategory
        );
    }
}
