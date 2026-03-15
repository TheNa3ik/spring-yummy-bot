package com.thena3ik.mealplanner.service;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class GeminiService {

    private final Client geminiClient;

    public GeminiService(@Value("${gemini.api.key}") String apiKey) {
        this.geminiClient = Client.builder()
                .apiKey(apiKey)
                .build();
    }

    @Async
    public CompletableFuture<String> translateTextAsync(String text, String targetLang) {
        String prompt = "Translate this text to " + targetLang + " without any comments or markdown: " + text;

        GenerateContentResponse response = geminiClient.models.generateContent(
                "gemini-3.1-flash-lite-preview",
                prompt,
                null);

        return CompletableFuture.completedFuture(response.text());
    }
}
