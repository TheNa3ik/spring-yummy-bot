package com.thena3ik.mealplanner.models;

import java.util.Optional;

public enum Diet {
    NONE("🥩 None", "none"),
    VEGETARIAN("🥗 Vegetarian", "vegetarian"),
    VEGAN("🌾 Vegan", "vegan"),
    KETO("🧀 Keto", "keto");

    private final String displayText;
    private final String apiValue;

    Diet(String displayText, String apiValue) {
        this.displayText = displayText;
        this.apiValue = apiValue;
    }

    public String getDisplayText() {
        return displayText;
    }

    public String getApiValue() {
        return apiValue;
    }

    public static Optional<Diet> fromDisplayText(String text) {
        if (text == null) return Optional.empty();
        for (Diet diet : values()) {
            if (diet.getDisplayText().equals(text.trim())) {
                return Optional.of(diet);
            }
        }
        return Optional.empty();
    }
}