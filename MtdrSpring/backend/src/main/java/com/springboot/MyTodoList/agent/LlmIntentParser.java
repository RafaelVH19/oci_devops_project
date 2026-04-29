package com.springboot.MyTodoList.agent;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.springboot.MyTodoList.config.AiProps;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class LlmIntentParser implements IntentParser {

    private static final Logger logger = LoggerFactory.getLogger(LlmIntentParser.class);

    private final AiProps aiProps;
    private final ObjectMapper objectMapper;
    private final RuleBasedIntentParser fallbackParser;

    public LlmIntentParser(AiProps aiProps, ObjectMapper objectMapper, RuleBasedIntentParser fallbackParser) {
        this.aiProps = aiProps;
        this.objectMapper = objectMapper;
        this.fallbackParser = fallbackParser;
    }

    @Override
    public ParsedIntent parse(String messageText) {
        if (!aiProps.isEnabled() || aiProps.getApiKey() == null || aiProps.getApiKey().isBlank()) {
            return fallbackParser.parse(messageText);
        }

        try {
            ParsedIntent parsedIntent = requestIntentClassification(messageText);
            if (parsedIntent == null) {
                return fallbackParser.parse(messageText);
            }

            if (parsedIntent.isClarificationNeeded()) {
                return parsedIntent;
            }

            if (parsedIntent.getIntent() == IntentType.UNKNOWN || parsedIntent.getIntent() == IntentType.GUACAMOLE) {
                String responseText = requestGeneralResponse(messageText);
                if (responseText != null && !responseText.isBlank()) {
                    parsedIntent.setResponseText(responseText);
                    return parsedIntent;
                }
            }

            return parsedIntent;
        } catch (Exception ex) {
            logger.warn("Fallo el parser LLM. Uso fallback local.", ex);
            return fallbackParser.parse(messageText);
        }
    }

    private ParsedIntent requestIntentClassification(String messageText) throws Exception {
        RestClient client = createClient();
        String endpoint = aiProps.getBaseUrl().replaceAll("/$", "") + "/chat/completions";

        String systemPrompt = "Eres un clasificador de intenciones para un asistente de gestion agile.\n"
            + "Debes responder solo JSON valido, sin bloques de codigo ni texto extra.\n"
            + "Intenciones permitidas:\n"
            + "HELP\n"
            + "LIST_TASKS\n"
            + "LIST_TASKS_BY_ASSIGNEE\n"
            + "LIST_TASKS_BY_STATUS\n"
            + "CREATE_TASK\n"
            + "CURRENT_SPRINT_SUMMARY\n"
            + "TEAM_LOAD_SUMMARY\n"
            + "GUACAMOLE\n"
            + "UNKNOWN\n\n"
            + "Devuelve JSON con:\n"
            + "intent, assignee, status, title, storyPoints, sprintName, clarificationNeeded, clarificationQuestion.\n"
            + "Si falta informacion importante, pide aclaracion.";

        Map<String, Object> systemMsg = new HashMap<>();
        systemMsg.put("role", "system");
        systemMsg.put("content", systemPrompt);

        Map<String, Object> userMsg = new HashMap<>();
        userMsg.put("role", "user");
        userMsg.put("content", messageText == null ? "" : messageText);

        List<Map<String, Object>> messages = List.of(systemMsg, userMsg);

        Map<String, Object> payload = new HashMap<>();
        payload.put("model", aiProps.getModel());
        payload.put("messages", messages);
        payload.put("temperature", 0);
        payload.put("response_format", Map.of("type", "json_object"));

        String responseBody = client.post()
            .uri(endpoint)
            .body(payload)
            .retrieve()
            .body(String.class);

        JsonNode root = objectMapper.readTree(responseBody);
        JsonNode content = root.path("choices").path(0).path("message").path("content");
        if (content.isMissingNode() || content.asText().isBlank()) {
            return null;
        }

        String jsonPayload = extractJsonPayload(content.asText());
        if (jsonPayload == null || jsonPayload.isBlank()) {
            return null;
        }

        return objectMapper.readValue(jsonPayload, ParsedIntent.class);
    }

    private String requestGeneralResponse(String messageText) throws Exception {
        RestClient client = createClient();
        String endpoint = aiProps.getBaseUrl().replaceAll("/$", "") + "/chat/completions";

        String systemPrompt = "Eres un asistente general en espanol.\n"
            + "Responde de forma breve, clara y util a cualquier pregunta del usuario.\n"
            + "No uses bloques de codigo ni texto extra.\n"
            + "Si hace falta, da pasos concretos.";

        Map<String, Object> systemMsg = new HashMap<>();
        systemMsg.put("role", "system");
        systemMsg.put("content", systemPrompt);

        Map<String, Object> userMsg = new HashMap<>();
        userMsg.put("role", "user");
        userMsg.put("content", messageText == null ? "" : messageText);

        List<Map<String, Object>> messages = List.of(systemMsg, userMsg);

        Map<String, Object> payload = new HashMap<>();
        payload.put("model", aiProps.getModel());
        payload.put("messages", messages);
        payload.put("temperature", 0.2);

        String responseBody = client.post()
            .uri(endpoint)
            .body(payload)
            .retrieve()
            .body(String.class);

        JsonNode root = objectMapper.readTree(responseBody);
        JsonNode content = root.path("choices").path(0).path("message").path("content");
        if (content.isMissingNode() || content.asText().isBlank()) {
            return null;
        }

        return content.asText().trim();
    }

    private RestClient createClient() {
        return RestClient.builder()
            .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + aiProps.getApiKey())
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build();
    }

    private String extractJsonPayload(String content) {
        String trimmed = content == null ? "" : content.trim();
        if (trimmed.isEmpty()) {
            return trimmed;
        }

        if (trimmed.startsWith("```")) {
            int firstNewLine = trimmed.indexOf('\n');
            if (firstNewLine >= 0) {
                trimmed = trimmed.substring(firstNewLine + 1).trim();
            }

            if (trimmed.endsWith("```")) {
                trimmed = trimmed.substring(0, trimmed.length() - 3).trim();
            }
        }

        int jsonStart = findJsonStart(trimmed);
        if (jsonStart < 0) {
            return trimmed;
        }

        int jsonEnd = findJsonEnd(trimmed, jsonStart);
        if (jsonEnd < 0) {
            return trimmed.substring(jsonStart);
        }

        return trimmed.substring(jsonStart, jsonEnd + 1);
    }

    private int findJsonStart(String content) {
        for (int index = 0; index < content.length(); index++) {
            if (content.charAt(index) == '{') {
                return index;
            }
        }
        return -1;
    }

    private int findJsonEnd(String content, int startIndex) {
        int depth = 0;
        boolean inString = false;
        boolean escaped = false;

        for (int index = startIndex; index < content.length(); index++) {
            char current = content.charAt(index);

            if (inString) {
                if (escaped) {
                    escaped = false;
                } else if (current == '\\') {
                    escaped = true;
                } else if (current == '"') {
                    inString = false;
                }
                continue;
            }

            if (current == '"') {
                inString = true;
                continue;
            }

            if (current == '{') {
                depth++;
            } else if (current == '}') {
                depth--;
                if (depth == 0) {
                    return index;
                }
            }
        }

        return -1;
    }
}
