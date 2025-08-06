package com.research.project.configs;


import com.research.project.services.LLMService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class LLMServiceFactory {

    private final Map<String, LLMService> serviceMap;

    @Autowired
    public LLMServiceFactory(com.research.project.services.GeminiService geminiService, com.research.project.services.GrokService grokService, com.research.project.services.DeepSeekService deepSeekService) {
        serviceMap = Map.of(
                "gemini", geminiService,
                "grok", grokService,
                "deepseek", deepSeekService
                // Add other models here (e.g., GPT, Grok, DeepSeek)
        );
    }

    public LLMService getService(String model) {
        LLMService service = serviceMap.get(model.toLowerCase());
        if (service == null) {
            throw new IllegalArgumentException("Unsupported LLM model: " + model);
        }
        return service;
    }
}
