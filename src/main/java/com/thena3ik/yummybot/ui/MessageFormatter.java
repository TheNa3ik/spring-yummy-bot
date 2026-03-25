package com.thena3ik.yummybot.ui;

import com.thena3ik.yummybot.model.dto.RecipeView;
import com.thena3ik.yummybot.service.LocaleService;
import org.springframework.stereotype.Component;

@Component
public class MessageFormatter {

    private final LocaleService localeService;

    public MessageFormatter(LocaleService localeService) {
        this.localeService = localeService;
    }

    public String formatRecipeCard(RecipeView view, String lang) {
        return String.format(
                """
                🥣 *%s*
                %s
                %s %s   |   %s
                """,
                view.title(),
                buildInvisibleLink(view.imageUrl()),
                localeService.getMessage("flow.recipe.servings", lang),
                formatNumber(view.servings()),
                localeService.getMessage("flow.recipe.ready_in", lang,
                        formatNumber(view.readyInMinutes())));
    }

    public String formatRecipeDetails(RecipeView view, String lang) {
        return String.format("""
            🥣 *%s*
            %s %s
            %s
            %s
            %s
            
            %s
            %s
            """,
                view.title(),
                localeService.getMessage("flow.recipe.servings", lang),
                formatNumber(view.servings()),
                localeService.getMessage("flow.recipe.ready_in", lang,
                        formatNumber(view.readyInMinutes())),
                buildInvisibleLink(view.imageUrl()),
                view.summary(),
                localeService.getMessage("flow.recipe.ingredients", lang),
                view.ingredients()
        );
    }

    private String formatNumber(int value) {
        return value > 0 ? String.valueOf(value) : "?";
    }

    private String buildInvisibleLink(String imageUrl) {
        return (imageUrl != null && !imageUrl.isBlank()) ? String.format("[\u200B](%s)", imageUrl) : "";
    }

}