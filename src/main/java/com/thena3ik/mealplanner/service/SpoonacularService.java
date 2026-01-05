package com.thena3ik.mealplanner.service;

import com.thena3ik.mealplanner.models.Recipe;
import com.thena3ik.mealplanner.models.RecipeDetails;
import com.google.gson.Gson;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class SpoonacularService {

    private static final String SEARCH_URL = "https://api.spoonacular.com/recipes/complexSearch";
    private static final String DETAILS_URL = "https://api.spoonacular.com/recipes/%d/information";
    private final String apiKey;
    private final OkHttpClient client;
    private final Gson gson;

    @Autowired
    public SpoonacularService(@Value("${spoonacular.api.key}") String apiKey) {
        this.apiKey = apiKey;
        this.client = new OkHttpClient();
        this.gson = new Gson();
    }

    private static class RecipeSearchResponse {
        List<Recipe> results;
        int offset;
    }

    public Optional<Recipe> searchSingleRecipe(String ingredients, String diet, int offset) {
        try {
            String normalized = normalizeIngredients(ingredients);
            String dietValue = (diet == null || diet.isBlank()) ? "none" : diet.trim().toLowerCase(Locale.ROOT);

            HttpUrl url = HttpUrl.parse(SEARCH_URL).newBuilder()
                    .addQueryParameter("includeIngredients", normalized)
                    .addQueryParameter("diet", dietValue)
                    .addQueryParameter("number", "1")
                    .addQueryParameter("offset", String.valueOf(Math.max(0, offset)))
                    .addQueryParameter("addRecipeInformation", "true")
                    .addQueryParameter("apiKey", apiKey)
                    .build();

            Request request = new Request.Builder().url(url).get().build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) return Optional.empty();

                String body = response.body() == null ? "" : response.body().string();
                if (body.isBlank()) return Optional.empty();

                RecipeSearchResponse searchResponse = gson.fromJson(body, RecipeSearchResponse.class);

                if (searchResponse == null || searchResponse.results == null || searchResponse.results.isEmpty()) {
                    return Optional.empty();
                }

                return Optional.of(searchResponse.results.getFirst());
            }

        } catch (Exception e) {
            System.err.println("Spoonacular error: " + e.getMessage());
            return Optional.empty();
        }
    }

    public Optional<RecipeDetails> getRecipeDetails(int recipeId) {
        try {
            String url = String.format(DETAILS_URL, recipeId);

            HttpUrl httpUrl = HttpUrl.parse(String.format(DETAILS_URL, recipeId)).newBuilder()
                    .addQueryParameter("apiKey", apiKey)
                    .build();

            Request request = new Request.Builder().url(httpUrl).get().build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) return Optional.empty();

                String body = response.body() != null ? response.body().string() : "";
                if (body.isBlank()) return Optional.empty();

                RecipeDetails details = gson.fromJson(body, RecipeDetails.class);
                return Optional.ofNullable(details);
            }

        } catch (IOException e) {
            System.err.println("Recipe details error: " + e.getMessage());
            return Optional.empty();
        }
    }


    private String normalizeIngredients(String ingredients) {
        if (ingredients == null || ingredients.isBlank()) return "";
        return ingredients.trim().toLowerCase(Locale.ROOT)
                .replaceAll("[\\s,]+", ",")
                .replaceAll("^,|,$", "");
    }
}
