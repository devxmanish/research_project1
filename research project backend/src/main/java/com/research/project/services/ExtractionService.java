package com.research.project.services;

import com.research.project.configs.LLMServiceFactory;
import com.research.project.dtos.ExtractionRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@Service
public class ExtractionService {

    @Autowired private LLMServiceFactory llmServiceFactory;

    @Autowired
    private OpenRouterService openRouterService;

    public Map<String, Object> extractEntities(ExtractionRequest request) {
        try {
            // Convert file content to a string
            String fileContent = new String(request.getFile().getBytes(), StandardCharsets.UTF_8);

            System.out.println("Extraction started...");

//            LLMService llmService = llmServiceFactory.getService(request.getModel());
//            Map<String, Object> extractedData = llmService.extractEntities(
//                    fileContent,  // Send extracted text instead of file
//                    request.getModel(),
//                    request.getOptions().toArray(new String[0])
//            );

            Map<String, Object> extractedData = openRouterService.extractEntities(
                    fileContent,  // Send extracted text instead of file
                    "meta-llama/llama-4-maverick:free",
                    request.getOptions());


            System.out.println("Extraction completed.");
//            return extractedData;
            return Map.of();
        } catch (Exception e) {
            throw new RuntimeException("Error processing file", e);
        }
    }
}
