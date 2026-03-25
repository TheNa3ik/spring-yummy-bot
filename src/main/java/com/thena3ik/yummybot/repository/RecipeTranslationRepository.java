package com.thena3ik.yummybot.repository;

import com.thena3ik.yummybot.model.entity.RecipeTranslationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RecipeTranslationRepository extends JpaRepository<RecipeTranslationEntity, Integer> {
    Optional<RecipeTranslationEntity> findFirstByRecipeIdAndLanguageCode(int recipeId, String languageCode);
}
