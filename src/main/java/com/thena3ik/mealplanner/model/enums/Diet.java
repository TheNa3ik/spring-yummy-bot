package com.thena3ik.mealplanner.model.enums;

import lombok.Getter;

@Getter
public enum Diet {
    NONE("menu.diet.btn.none", "none"),
    VEGETARIAN("menu.diet.btn.vegetarian", "vegetarian"),
    VEGAN("menu.diet.btn.vegan", "vegan"),
    KETO("menu.diet.btn.keto", "keto");

    private final String labelText;
    private final String code;

    Diet(String labelText, String code) {
        this.labelText = labelText;
        this.code = code;
    }
}