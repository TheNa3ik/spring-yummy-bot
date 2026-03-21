package com.thena3ik.mealplanner.model.dto;

import com.google.gson.annotations.SerializedName;

public record Recipe(
        int id,
        String title,
        String image,
        int servings,
        @SerializedName("readyInMinutes") int readyInMinutes
) {}