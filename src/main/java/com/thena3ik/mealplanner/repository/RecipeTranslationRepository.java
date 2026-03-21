package com.thena3ik.mealplanner.repository;

import com.thena3ik.mealplanner.model.entity.RecipeTranslationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RecipeTranslationRepository extends JpaRepository<RecipeTranslationEntity, Integer> {
    Optional<RecipeTranslationEntity> findFirstByRecipeIdAndLanguageCode(int recipeId, String languageCode);
}
