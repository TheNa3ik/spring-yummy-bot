package com.thena3ik.yummybot.repository;

import com.thena3ik.yummybot.model.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

}
