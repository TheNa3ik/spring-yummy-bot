package com.thena3ik.mealplanner.models;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

@Getter
@Embeddable
public class LastSearch {
    @Setter
    private String diet;
    @Setter
    private String ingredients;
    @Setter
    private int recipeId;
    private int offset;

    public LastSearch() {
    }

    public LastSearch(String diet, String ingredients, int recipeId) {
        this.diet = diet;
        this.ingredients = ingredients;
        this.recipeId = recipeId;
        this.offset = 0;
    }

    public void incrementOffset() {
        this.offset++;
    }

    public void decrementOffset() {
        if (this.offset > 0) this.offset--;
    }

    public void resetOffset() {
        this.offset = 0;
    }
}


