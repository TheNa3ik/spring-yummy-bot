package com.thena3ik.mealplanner.repository;

import com.thena3ik.mealplanner.model.entity.RecipeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecipeRepository extends JpaRepository<RecipeEntity, Integer> {
}
