package com.research.project.services;

import java.util.Map;

public interface LLMService {
    Map<String, Object> extractEntities(String fileContent, String model, String[] options);
}
