package com.thena3ik.yummybot.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record RecipeView(
        String title,
        String summary,
        String ingredients,
        String imageUrl,
        int servings,
        int readyInMinutes
) {}
