package com.thena3ik.mealplanner.repository;

import com.thena3ik.mealplanner.models.user.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserDao extends JpaRepository<UserSession, Long> {

}
