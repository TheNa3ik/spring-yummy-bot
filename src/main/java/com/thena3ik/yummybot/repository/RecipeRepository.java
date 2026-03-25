package com.thena3ik.yummybot.repository;

import com.thena3ik.yummybot.model.entity.RecipeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecipeRepository extends JpaRepository<RecipeEntity, Integer> {
}
