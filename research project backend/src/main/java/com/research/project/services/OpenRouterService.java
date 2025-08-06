package com.research.project.services;


import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class OpenRouterService {

    @Autowired
    private ChatClient.Builder chatClientBuilder;

    public Map<String, Object> extractEntities(String userStories, String model, List<String> entities){
        ChatClient chatClient = chatClientBuilder.defaultOptions(
                ChatOptions.builder()
                        .model(model)
                        .temperature(0.2)
                        .build()
        ).build();

        var templete = """
                You are an expert at extracting structured elements from software user stories.
                
                ---
                
                ### Input:
                1. **User Stories**:
                {userStories}
                
                2. **Entities to Extract**: {entities}
                
                ---
                
                ### Task:
                Extract only the entities listed under **Entities to Extract** from the given user stories.
                
                #### Extraction Rules:
                
                **Classes**:
                - Identify nouns or noun phrases representing key components, actors, or objects.
                - Avoid duplicates or generic words.
                
                **Relationships**:
                - Format strictly as: `(Subject -> Predicate -> Object)`
                - Both **Subject** and **Object** must be from the extracted class list, if not then add if required.
                - **Predicate** should represent a meaningful action or link from the user story.
                - Use natural verbs (e.g., manages, creates, updates, views).
                
                **Attributes** (if requested):
                - Extract properties that describe a class.
                - Example: "User has name and email" → User: [name, email]
                
                **Operations** (if requested):
                - Extract actions performed by or on a class.
                - Format as method names (e.g., `createAccount()`, `sendNotification()`).
                
                ---
                
                ### Output Format:
                ```json
                {
                  "extractedEntities": {
                    "Classes": ["Class1", "Class2", ...],
                    "Relationships": [
                      "(Class1 -> action -> Class2)",
                      "(Class3 -> interactsWith -> Class4)"
                    ],
                    "Attributes": {
                      "Class1": ["attribute1", "attribute2"]
                    },
                    "Operations": {
                      "Class1": ["operation1()", "operation2()"]
                    }
                  }
                }
                """;

        PromptTemplate promptTemplete = new PromptTemplate(templete);
        Map<String , Object> params = Map.of(
                "userStories", userStories,
                "entities", entities
        );

        Prompt prompt = promptTemplete.create(params);

        ChatResponse chatResponse = chatClient.prompt().user(String.valueOf(prompt)).call().chatResponse();

        String response=  chatResponse.getResult().getOutput().getText();
        System.out.println(response);

        return Map.of();

    }
}
