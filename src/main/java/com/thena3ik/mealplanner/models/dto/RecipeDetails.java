package com.thena3ik.mealplanner.models.dto;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;

import java.util.List;

@Getter
public class RecipeDetails {
    private int id;
    private String title;
    private String summary;
    private int servings;
    @SerializedName("readyInMinutes")
    private int readyInMinutes;
    @SerializedName("extendedIngredients")
    private List<Ingredient> ingredients;
    private String image;
}