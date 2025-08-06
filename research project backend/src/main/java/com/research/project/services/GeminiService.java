package com.research.project.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GeminiService implements LLMService {
    private final WebClient webClient;
    private final String geminiApiKey = "AIzaSyCRP6SlpSDqD_JBqm5JnoF3cSLACV61n10"; // Replace with actual API key
    private final String geminiUrl = "https://generativelanguage.googleapis.com/v1/models/gemini-2.0-flash:generateContent?key=" + geminiApiKey;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public GeminiService() {
        this.webClient = WebClient.builder()
                .baseUrl(geminiUrl)
                .build();
    }

    @Override
    public Map<String, Object> extractEntities(String fileContent, String model, String[] options) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("contents", List.of(
                Map.of("role", "user",
                        "parts", List.of(Map.of("text", buildPrompt(fileContent, options))))
        ));

        try {
            String response = webClient.post()
                    .bodyValue(requestBody)
                    .retrieve()
                    .onStatus(status -> status.value() == 429, responseObj -> {
                        System.out.println("🔴 429 Too Many Requests – Retrying...");
                        return responseObj.createException();
                    })
                    .bodyToMono(String.class)
                    .retryWhen(Retry.backoff(3, Duration.ofSeconds(2))
                            .filter(e -> e instanceof WebClientResponseException.TooManyRequests))
                    .block(); // Blocking call to return response synchronously

            return processResponse(response, options);
        } catch (Exception e) {
            throw new RuntimeException("Error processing Gemini response", e);
        }
    }

//    private String buildPrompt(String fileContent, String[] options) {
//        return "Extract the following entities from this user story:\n\n"
//                + "User Story: " + fileContent + "\n\n"
//                + "Entities: " + String.join(", ", options) + "\n"
//                + "Format the response as a JSON object.";
//    }

//    private String buildPrompt(String fileContent, String[] options) {
//        return "Extract ONLY the following entities from the provided user story, ensuring they are relevant for software development and industry-level applications:\n\n"
//                + "User Story:\n" + fileContent + "\n\n"
//                + "Required Entities:\n" + String.join(", ", options) + "\n\n"
//                + "Strictly exclude any unrelated, redundant, or general terms. The output must be a well-structured JSON object containing only the requested entities.";
//    }

    private String buildPrompt(String fileContent, String[] options) {
        return "You are an AI designed to analyze multiple user stories and extract only the most relevant entities for software development and industry applications.\n\n"
                + "### Instructions:\n"
                + "1. **Read and analyze all user stories** provided below.\n"
                + "2. **Extract ONLY the following entities**, ensuring they are meaningful and distinct:\n"
                + String.join(", ", options) + "\n\n"
                + "3. **Merge similar entities into a single, standardized term.** For example:\n"
                + "   - 'Teacher' and 'Faculty Member' should be merged as 'Teacher'.\n"
                + "   - 'AI Model' and 'Machine Learning Model' should be merged as 'AI Model'.\n"
                + "4. **If an entity is implied across multiple user stories but not explicitly mentioned, include it.**\n"
                + "5. **Remove irrelevant, redundant, or general terms that do not contribute to system design or functionality.**\n\n"
                + "### User Stories:\n" + fileContent + "\n\n"
                + "### Response Format:\n"
                + "Provide the extracted entities as a structured JSON object with NO duplicate values and properly categorized items.";
    }



    private Map<String, Object> processResponse(String jsonResponse, String[] selectedEntities) {
        Map<String, Object> responseMap = new HashMap<>();
        Map<String, Object> extractedData = parseJsonResponse(jsonResponse, selectedEntities);
        responseMap.put("extractedEntities", extractedData);
        return responseMap;
    }

    private Map<String, Object> parseJsonResponse(String jsonResponse, String[] selectedEntities) {
        System.out.println("The selected entitys: ");
        for (String selectedEntity : selectedEntities) {
            System.out.println(selectedEntity);
        }
        Map<String, Object> parsedData = new HashMap<>();
        try {
            JsonNode rootNode = objectMapper.readTree(jsonResponse);
            JsonNode candidatesNode = rootNode.path("candidates");
            if (candidatesNode.isArray() && candidatesNode.size() > 0) {
                JsonNode contentNode = candidatesNode.get(0).path("content").path("parts").get(0).path("text");
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
