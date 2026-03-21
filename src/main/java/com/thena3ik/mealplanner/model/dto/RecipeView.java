package com.thena3ik.mealplanner.model.dto;

public record RecipeView(
        String title,
        String summary,
        String ingredients,
        String imageUrl,
        int servings,
        int readyInMinutes
) {}
