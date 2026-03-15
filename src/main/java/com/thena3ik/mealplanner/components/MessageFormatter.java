package com.thena3ik.mealplanner.components;

import com.thena3ik.mealplanner.models.entity.RecipeEntity;
import com.thena3ik.mealplanner.models.entity.RecipeTranslationEntity;
import com.thena3ik.mealplanner.service.LocaleService;
import org.springframework.stereotype.Component;

@Component
public class MessageFormatter {

    private final LocaleService localeService;

    public MessageFormatter(LocaleService localeService) {
        this.localeService = localeService;
    }

    public String formatRecipeCard(RecipeEntity recipe, RecipeTranslationEntity translation, String lang) {
        return String.format(
                """
                🥣 *%s*
                %s
                %s %s   |   %s
                """,
                resolveTitle(recipe, translation, lang),
                buildInvisibleLink(recipe),
                localeService.getMessage("recipe.servings", lang),
                formatNumber(recipe.getServings()),
                localeService.getMessage("recipe.readyIn", lang,
                        formatNumber(recipe.getReadyInMinutes())));
    }

    public String formatRecipeDetails(RecipeEntity recipe, RecipeTranslationEntity translation, String lang) {
        String noSummaryLabel = localeService.getMessage("recipe.no_summary", lang);
        String noIngredientsLabel = localeService.getMessage("recipe.no_ingredients", lang);

        return String.format("""
            🥣 *%s*
            %s %s
            %s
            %s
            %s
            
            %s
            %s
            """,
                resolveTitle(recipe, translation, lang),
                localeService.getMessage("recipe.servings", lang),
                formatNumber(recipe.getServings()),
                localeService.getMessage("recipe.readyIn", lang,
                        formatNumber(recipe.getReadyInMinutes())),
                buildInvisibleLink(recipe),
                resolveSummary(recipe, translation, lang, noSummaryLabel),
                localeService.getMessage("recipe.ingredients", lang),
                resolveIngredients(recipe, translation, lang, noIngredientsLabel)
        );
    }

    private String formatNumber(int value) {
        return value > 0 ? String.valueOf(value) : "?";
    }

    private String buildInvisibleLink(RecipeEntity recipe) {
        String imageUrl = recipe.getImage();
        return (imageUrl != null && !imageUrl.isBlank()) ? String.format("[\u200B](%s)", imageUrl) : "";
    }

    private String resolveTitle(RecipeEntity recipe, RecipeTranslationEntity translation, String lang) {
        return "en".equalsIgnoreCase(lang) ? recipe.getTitle() : translation.getTitle();
    }

    private String resolveSummary(RecipeEntity recipe, RecipeTranslationEntity translation, String lang, String fallback) {
        String summary = "en".equalsIgnoreCase(lang) ? recipe.getSummary() : translation.getSummary();
        return summary != null ? summary.replaceAll("<.*?>", "") : fallback;
    }

    private String resolveIngredients(RecipeEntity recipe, RecipeTranslationEntity translation, String lang, String fallback) {
        if (!"en".equalsIgnoreCase(lang)) {
            return translation.getTranslatedIngredients() != null ? translation.getTranslatedIngredients() : fallback;
        }

        if (recipe.getIngredients() == null || recipe.getIngredients().isEmpty()) {
            return fallback;
        }

        StringBuilder sb = new StringBuilder();
        for (String formattedIngredient : recipe.getIngredients()) {
            sb.append("🔹 ").append(formattedIngredient).append("\n");
        }
        return sb.toString().trim();
    }
}