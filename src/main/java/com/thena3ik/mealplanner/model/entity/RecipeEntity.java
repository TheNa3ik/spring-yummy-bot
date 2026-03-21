package com.thena3ik.mealplanner.model.entity;

import com.thena3ik.mealplanner.model.dto.RecipeDetails;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.stream.Collectors;

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

    @Column(name = "ingredients_list", columnDefinition = "TEXT")
    private String ingredientsList;

    private boolean detailsFetched = false;

    public RecipeEntity(RecipeDetails details) {
        this.id = details.id();
        updateWithDetails(details);
    }

    public void updateWithDetails(RecipeDetails details) {
        this.setReadyInMinutes(details.readyInMinutes());
        this.setServings(details.servings());
        this.setSummary(details.summary());

        if (details.ingredients() != null) {
            this.ingredientsList = details.ingredients().stream()
                    .map(ing -> {
                        String cleanName = ing.name().substring(0, 1).toUpperCase() + ing.name().substring(1);

                        String amountStr = "";
                        if (ing.measures() != null) {
                            amountStr = ": " + ing.measures().metric().amount() + " " + ing.measures().metric().unit();
                        }

                        return cleanName + amountStr;
                    })
                    .collect(Collectors.joining("\n"));
        }
        this.setDetailsFetched(true);
    }
}
