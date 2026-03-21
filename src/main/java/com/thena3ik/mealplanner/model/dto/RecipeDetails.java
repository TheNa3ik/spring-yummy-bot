package com.thena3ik.mealplanner.model.dto;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public record RecipeDetails(
        int id,
        String title,
        String summary,
        int servings,
        @SerializedName("readyInMinutes") int readyInMinutes,
        @SerializedName("extendedIngredients") List<Ingredient> ingredients,
        String image
) {}