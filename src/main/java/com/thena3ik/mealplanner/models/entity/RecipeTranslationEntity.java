package com.thena3ik.mealplanner.models.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "recipe_translations", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"recipe_id", "language_code"})
})
@Getter
@Setter
public class RecipeTranslationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "recipe_id")
    private int recipeId;

    @Column(name = "language_code")
    private String languageCode;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Column(columnDefinition = "TEXT")
    private String translatedIngredients;

    private boolean isDetailsTranslated = false;
}