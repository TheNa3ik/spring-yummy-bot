package com.thena3ik.yummybot.model.entity;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipe_id")
    private RecipeEntity recipe;

    @Column(name = "language_code")
    private String languageCode;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Column(columnDefinition = "TEXT")
    private String translatedIngredients;

    private boolean isDetailsTranslated = false;
}