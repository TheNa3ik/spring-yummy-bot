package com.thena3ik.mealplanner.models;

import lombok.Getter;

@Getter
public enum Diet {
    NONE("diet.button.none", "none"),
    VEGETARIAN("diet.button.vegetarian", "vegetarian"),
    VEGAN("diet.button.vegan", "vegan"),
    KETO("diet.button.keto", "keto");

    private final String labelKey;
    private final String apiValue;

    Diet(String labelKey, String apiValue) {
        this.labelKey = labelKey;
        this.apiValue = apiValue;
    }
}