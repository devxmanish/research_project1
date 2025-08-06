package com.research.project.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.*;

@Service
public class GrokService implements LLMService {
    private final WebClient webClient;
    private final String grokApiKey = API_KEY;
    private final String grokUrl = "https://api.x.ai/v1/chat/completions";
    private final ObjectMapper objectMapper = new ObjectMapper();

    public GrokService() {
        this.webClient = WebClient.builder()
                .baseUrl(grokUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + grokApiKey)
                .build();
    }

    @Override
    public Map<String, Object> extractEntities(String fileContent, String model, String[] options) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model); // Use passed model instead of hardcoded
        requestBody.put("temperature", 0);
        requestBody.put("stream", false);

        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", "You are an AI assistant that extracts structured entities from user stories."));
        messages.add(Map.of("role", "user", "content", buildPrompt(fileContent, options)));

        requestBody.put("messages", messages);

        try {
            String response = webClient.post()
                    .bodyValue(requestBody)
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError(), responseObj -> {
                        System.out.println("❌ Client error: " + responseObj.statusCode());
                        return responseObj.createException();
                    })
                    .onStatus(status -> status.value() == 429, responseObj -> {
                        System.out.println("🔁 429 Too Many Requests – Retrying...");
                        return responseObj.createException();
                    })
                    .bodyToMono(String.class)
                    .retryWhen(Retry.backoff(3, Duration.ofSeconds(2))
                            .filter(e -> e instanceof WebClientResponseException.TooManyRequests))
                    .block();

            return processResponse(response, options);

        } catch (WebClientResponseException e) {
            System.out.println("❌ Error Response (" + e.getStatusCode() + "): " + e.getResponseBodyAsString());
            throw new RuntimeException("API request failed: " + e.getStatusCode(), e);
        } catch (Exception e) {
            throw new RuntimeException("Error processing Grok response", e);
        }
    }

    private String buildPrompt(String fileContent, String[] options) {
        return "Extract ONLY the following entities from the provided user story, ensuring they are relevant for software development and industry-level applications:\n\n"
                + "User Story:\n" + fileContent + "\n\n"
                + "Required Entities:\n" + String.join(", ", options) + "\n\n"
                + "Strictly exclude any unrelated, redundant, or general terms. The output must be a well-structured JSON object containing only the requested entities.";
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
            JsonNode choicesNode = rootNode.path("choices");
            if (choicesNode.isArray() && choicesNode.size() > 0) {
                JsonNode contentNode = choicesNode.get(0).path("message").path("content");
                String jsonText = contentNode.asText().replace("```json", "").replace("```", "").trim();
                JsonNode extractedEntitiesNode = objectMapper.readTree(jsonText);

                for (String entity : selectedEntities) {
                    JsonNode entityNode = extractedEntitiesNode.path(entity);
                    if (entityNode.isArray()) {
                        List<String> entityValues = objectMapper.convertValue(entityNode, List.class);
                        parsedData.put(entity, entityValues);
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Error parsing LLM response JSON", e);
        }
        return parsedData;
    }
}
