package com.thena3ik.mealplanner.service;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Slf4j
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
        if (text == null || text.isBlank()) {
            log.warn("Attempted to translate empty or null text to '{}'. Skipping API call.", targetLang);
            return CompletableFuture.completedFuture("");
        }

        try {
            String prompt = "Translate this text to " + targetLang + " without any comments or markdown: " + text;

            GenerateContentResponse response = geminiClient.models.generateContent(
                    "gemini-3.1-flash-lite-preview",
                    prompt,
                    null);

            return CompletableFuture.completedFuture(response.text());

        } catch (Exception e) {
            log.error("Gemini translation API failed for text snippet. Target lang: '{}'", targetLang, e);

            return CompletableFuture.completedFuture(text);
        }
    }
}
