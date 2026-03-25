package com.thena3ik.yummybot.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Recipe(
        int id,
        String title,
        String summary,
        int servings,
        @JsonProperty("readyInMinutes") int readyInMinutes,
        @JsonProperty("extendedIngredients") List<Ingredient> ingredients,
        String image
) {}
