package com.research.project.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class DeepSeekService implements LLMService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String apiKey = API_KEY; // Replace with your actual OpenRouter API key
    private final String apiUrl = "https://openrouter.ai/api/v1/chat/completions";

    @Override
    public Map<String, Object> extractEntities(String fileContent, String model, String[] options) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);
        headers.set("HTTP-Referer", "https://yourdomain.com"); // optional
        headers.set("X-Title", "Entity Extractor"); // optional

        Map<String, Object> payload = new HashMap<>();
        payload.put("model", "deepseek/deepseek-r1:free"); // e.g., "openai/gpt-3.5-turbo"
        payload.put("temperature", 0.7);
        payload.put("max_tokens", 1000);

        List<Map<String, String>> messages = List.of(
                Map.of("role", "system", "content", "You are an AI that extracts software development entities."),
                Map.of("role", "user", "content", buildPrompt(fileContent, options))
        );

        payload.put("messages", messages);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, request, String.class);
            return processResponse(response.getBody(), options);
        } catch (Exception e) {
            throw new RuntimeException("Error calling OpenRouter API", e);
        }
    }

    private String buildPrompt(String fileContent, String[] options) {
        return "Analyze the following user stories and extract the following entities: "
                + String.join(", ", options) + ".\n\n"
                + "User Stories:\n" + fileContent + "\n\n"
                + "Return a clean JSON object with those entities only.";
    }

    private Map<String, Object> processResponse(String jsonResponse, String[] selectedEntities) {
        Map<String, Object> responseMap = new HashMap<>();
        Map<String, Object> extractedData = parseJsonResponse(jsonResponse, selectedEntities);
        responseMap.put("extractedEntities", extractedData);
        return responseMap;
    }

    private Map<String, Object> parseJsonResponse(String jsonResponse, String[] selectedEntities) {
        Map<String, Object> parsedData = new HashMap<>();
        try {
            JsonNode rootNode = objectMapper.readTree(jsonResponse);
            JsonNode contentNode = rootNode.path("choices").get(0).path("message").path("content");
            String jsonText = contentNode.asText().replace("```json", "").replace("```", "").trim();
            JsonNode extractedEntitiesNode = objectMapper.readTree(jsonText);

            for (String entity : selectedEntities) {
                JsonNode entityNode = extractedEntitiesNode.path(entity);
                if (entityNode.isArray()) {
                    List<String> entityValues = objectMapper.convertValue(entityNode, List.class);
                    parsedData.put(entity, entityValues);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Error parsing response JSON", e);
        }
        return parsedData;
    }
}
