package com.thena3ik.mealplanner.models.entity;

import com.thena3ik.mealplanner.models.dto.RecipeDetails;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "recipes")
@Getter
@Setter
@NoArgsConstructor
@SuppressWarnings("unused")
public class RecipeEntity {

    @Id
    private int id;
    private String title;
    private String image;
    private int servings;
    private int readyInMinutes;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "recipe_ingredients", joinColumns = @JoinColumn(name = "recipe_id"))
    @Column(name = "ingredient_name")
    private List<String> ingredients;

    private boolean detailsFetched = false;

    public RecipeEntity(RecipeDetails details) {
        this.id = details.getId();
        updateWithDetails(details);
    }

    public void updateWithDetails(RecipeDetails details) {
        this.title = details.getTitle();
        this.image = details.getImage();
        this.servings = details.getServings();
        this.readyInMinutes = details.getReadyInMinutes();
        this.summary = details.getSummary();

        if (details.getIngredients() != null) {
            this.ingredients = details.getIngredients().stream().map(ingredient -> {
                String name = ingredient.getName();
                double amount = ingredient.getMeasures().getMetric().getAmount();
                String unit = ingredient.getMeasures().getMetric().getUnit();

                String amountStr = (amount == (long) amount) ?
                        String.format("%d", (long) amount) : String.format("%.1f", amount);
                String formattedName = name.substring(0, 1).toUpperCase() + name.substring(1);

                String finalString = "**" + formattedName + "**: " + amountStr;
                return (unit != null && !unit.isBlank()) ? finalString + " " + unit : finalString;
            }).toList();
        }
        this.detailsFetched = true;
    }
}
