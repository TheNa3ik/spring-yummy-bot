package com.thena3ik.yummybot.service;

import com.thena3ik.yummybot.model.dto.RandomRecipeResponse;
import com.thena3ik.yummybot.model.dto.Recipe;
import com.thena3ik.yummybot.model.dto.RecipeSearchResponse;
import com.thena3ik.yummybot.model.entity.RecipeEntity;
import com.thena3ik.yummybot.model.entity.SearchStateEntity;
import com.thena3ik.yummybot.model.entity.UserEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Locale;
import java.util.Optional;

@Slf4j
@Service
public class SpoonacularService {

    private final RestClient restClient;

    @Value("${spoonacular.api.key}")
    private String apiKey;

    public SpoonacularService() {
        this.restClient = RestClient.builder()
                .baseUrl("https://api.spoonacular.com")
                .build();
    }

    public Optional<RecipeEntity> searchByIngredients(UserEntity session) {
        SearchStateEntity state = session.getSearchState();
        String rawIngredients = state.getIngredients();

        Optional<RecipeEntity> resultOpt = executeComplexSearch(session,
                "includeIngredients", normalizeIngredients(rawIngredients));

        if (resultOpt.isPresent()) {
            RecipeEntity recipe = resultOpt.get();
            String validationText = recipe.getIngredientsList();

            if (validationText == null || validationText.isBlank()) {
                validationText = recipe.getTitle() + " " + recipe.getSummary();
            }

            if (validationText != null && !validationText.isBlank()) {
                validationText = validationText.toLowerCase(Locale.ROOT);
                String[] userIngredients = rawIngredients.toLowerCase(Locale.ROOT).split(",");

                boolean hasValidMatch = false;

                for (String input : userIngredients) {
                    String term = input.trim();
                    if (term.isEmpty()) continue;

                    String singular = term.endsWith("s") ? term.substring(0, term.length() - 1) : term;

                    if (validationText.contains(term) || validationText.contains(singular)) {
                        hasValidMatch = true;
                        break;
                    }
                }

                if (!hasValidMatch) {
                    log.warn("Spoonacular returned a false positive for invalid ingredients '{}'. Rejecting.", rawIngredients);
                    return Optional.empty();
                }
            }
        }
        return resultOpt;
    }

    public Optional<RecipeEntity> searchByName(UserEntity session) {
        SearchStateEntity state = session.getSearchState();
        return executeComplexSearch(session, "query", state.getSearchName());
    }

    public Optional<RecipeEntity> getRandomRecipe(UserEntity session) {
        SearchStateEntity state = session.getSearchState();
        String tags = buildTags(session.getDiet(), state.getCuisine());

        try {
            RandomRecipeResponse response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/recipes/random")
                            .queryParam("number", 1)
                            .queryParam("tags", tags)
                            .queryParam("apiKey", apiKey)
                            .build())
                    .retrieve()
                    .body(RandomRecipeResponse.class);

            if (response != null && response.recipes() != null && !response.recipes().isEmpty()) {
                return Optional.of(new RecipeEntity(response.recipes().getFirst()));
            }
        } catch (Exception e) {
            log.error("Failed to fetch random recipe", e);
        }
        return Optional.empty();
    }

    public Optional<Recipe> getRecipeDetails(int recipeId) {
        try {
            Recipe response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/recipes/{id}/information")
                            .queryParam("apiKey", apiKey)
                            .build(recipeId))
                    .retrieve()
                    .body(Recipe.class);

            return Optional.ofNullable(response);

        } catch (Exception e) {
            log.error("Failed to fetch full recipe information for ID: {}", recipeId, e);
            return Optional.empty();
        }
    }

    private Optional<RecipeEntity> executeComplexSearch(UserEntity session, String specificParamKey, String specificParamValue) {
        SearchStateEntity state = session.getSearchState();
        String dietValue = (session.getDiet() == null || session.getDiet().isBlank()) ? "" : session.getDiet().trim().toLowerCase(Locale.ROOT);

        try {
            RecipeSearchResponse response = restClient.get()
                    .uri(uriBuilder -> {
                        uriBuilder.path("/recipes/complexSearch")
                                .queryParam("number", 1)
                                .queryParam("addRecipeInformation", true)
                                .queryParam("offset", Math.max(0, state.getOffset()))
                                .queryParam("apiKey", apiKey)
                                .queryParam(specificParamKey, specificParamValue);

                        if (!dietValue.isEmpty() && !dietValue.equals("none")) {
                            uriBuilder.queryParam("diet", dietValue);
                        }
                        if (state.getCuisine() != null && !state.getCuisine().isBlank()) {
                            uriBuilder.queryParam("cuisine", state.getCuisine());
                        }
                        return uriBuilder.build();
                    })
                    .retrieve()
                    .body(RecipeSearchResponse.class);

            if (response != null && response.results() != null && !response.results().isEmpty()) {
                return Optional.of(new RecipeEntity(response.results().getFirst()));
            }
        } catch (Exception e) {
            log.error("Spoonacular complex search failed", e);
        }
        return Optional.empty();
    }

    private String buildTags(String diet, String cuisine) {
        StringBuilder tags = new StringBuilder();
        if (diet != null && !diet.equalsIgnoreCase("none")) tags.append(diet).append(",");
        if (cuisine != null && !cuisine.isBlank()) tags.append(cuisine);
        return tags.toString().toLowerCase(Locale.ROOT).replaceAll(",$", "");
    }

    private String normalizeIngredients(String ingredients) {
        if (ingredients == null) return "";
        return ingredients.trim().replaceAll("\\s+", " ").replace(", ", ",");
    }
}