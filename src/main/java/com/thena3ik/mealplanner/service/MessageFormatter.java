package com.thena3ik.mealplanner.service;

import com.thena3ik.mealplanner.models.Ingredient;
import com.thena3ik.mealplanner.models.Recipe;
import com.thena3ik.mealplanner.models.RecipeDetails;
import org.springframework.stereotype.Component;

@Component
public class MessageFormatter {

    public String formatRecipeCard(Recipe recipe) {
        String servingsStr = recipe.getServings() > 0 ? String.valueOf(recipe.getServings()) : "?";
        String readyInStr = recipe.getReadyInMinutes() > 0 ? String.valueOf(recipe.getReadyInMinutes()) : "?";

        return String.format(
                """
                        🥣 *%s*
                        [\u200B](%s)
                        🧑‍🍳 Servings: %s   |   ⏰ Ready in: %s min
                        """,
                recipe.getTitle(),
                recipe.getImage(),
                servingsStr,
                readyInStr
        );
    }

    public String formatRecipeDetails(RecipeDetails details) {
        String title = details.getTitle();
        String summary = details.getSummary() != null ? details.getSummary().replaceAll("<.*?>", "") : "No summary.";
        String servings = details.getServings() > 0 ? String.valueOf(details.getServings()) : "?";
        String readyIn = details.getReadyInMinutes() > 0 ? String.valueOf(details.getReadyInMinutes()) : "?";
        String imageUrl = details.getImage();

        StringBuilder ingredientsListBuilder = new StringBuilder("\n🛒 *Ingredients*:\n");
        if (details.getIngredients() == null || details.getIngredients().isEmpty()) {
            ingredientsListBuilder.append("No ingredients listed.\n");
        } else {
            for (Ingredient ingredient : details.getIngredients()) {
                String name = ingredient.getName();
                double amount = ingredient.getMeasures().getMetric().getAmount();
                String unit = ingredient.getMeasures().getMetric().getUnit();

                String amountStr;
                if (amount == (long) amount) {
                    amountStr = String.format("%d", (long) amount);
                } else {
                    amountStr = String.format("%.1f", amount);
                }

                String formattedName = name.substring(0, 1).toUpperCase() + name.substring(1);

                ingredientsListBuilder.append("\uD83D\uDD39 **").append(formattedName).append("**: ").append(amountStr);
                if (unit != null && !unit.isBlank()) {
                    ingredientsListBuilder.append(" ").append(unit);
                }
                ingredientsListBuilder.append("\n");
            }
        }

        String invisibleLink = "";
        if (imageUrl != null && !imageUrl.isBlank()) {
            invisibleLink = String.format("[\u200B](%s)", imageUrl);
        }

        return String.format("""
            🥣 *%s*
            🧑‍🍳 Servings: %s
            ⏰ Ready in: %s min
            %s
            %s
            %s
            """, title, servings, readyIn, invisibleLink, summary, ingredientsListBuilder.toString());
    }
}