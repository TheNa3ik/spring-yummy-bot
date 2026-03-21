package com.thena3ik.mealplanner.repository;

import com.thena3ik.mealplanner.model.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

}
