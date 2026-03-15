package com.thena3ik.mealplanner.repository;

import com.thena3ik.mealplanner.models.entity.RecipeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecipeDao extends JpaRepository<RecipeEntity, Integer> {
}
