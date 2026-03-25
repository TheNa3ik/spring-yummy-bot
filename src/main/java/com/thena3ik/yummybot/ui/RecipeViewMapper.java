package com.thena3ik.yummybot.ui;

import com.thena3ik.yummybot.model.dto.RecipeView;
import com.thena3ik.yummybot.model.entity.RecipeEntity;
import com.thena3ik.yummybot.model.entity.RecipeTranslationEntity;
import com.thena3ik.yummybot.service.LocaleService;
import org.springframework.stereotype.Component;

@Component
public class RecipeViewMapper {
    private final LocaleService localeService;

    public RecipeViewMapper(LocaleService localeService) {
        this.localeService = localeService;
    }

    public RecipeView map(RecipeEntity recipe, RecipeTranslationEntity translation, String lang) {
        String title = (translation != null && translation.getTitle() != null && !translation.getTitle().isBlank())
                ? translation.getTitle() : recipe.getTitle();

        String rawSummary = (translation != null && translation.getSummary() != null && !translation.getSummary().isBlank())
                ? translation.getSummary() : recipe.getSummary();
        String summary = (rawSummary != null) ? rawSummary.replaceAll("<.*?>", "")
                : localeService.getMessage("flow.recipe.error.no_summary", lang);

        String ingredients = localeService.getMessage("flow.recipe.error.no_ingredients", lang);

        if (translation != null && translation.getTranslatedIngredients() != null && !translation.getTranslatedIngredients().isBlank()) {
            StringBuilder sb = new StringBuilder();
            for (String line : translation.getTranslatedIngredients().split("\n")) {
                sb.append("🔹 ").append(line.trim()).append("\n");
            }
            ingredients = sb.toString().trim();

        } else if (recipe.getIngredientsList() != null && !recipe.getIngredientsList().isEmpty()) {
            ingredients = java.util.Arrays.stream(recipe.getIngredientsList().split("\n"))
                    .map(line -> "🔹 " + line)
                    .collect(java.util.stream.Collectors.joining("\n"));
        }

        return new RecipeView(title, summary, ingredients, recipe.getImage(), recipe.getServings(), recipe.getReadyInMinutes());
    }
}
