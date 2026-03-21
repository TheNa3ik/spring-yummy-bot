package com.thena3ik.mealplanner.bot.handler.interceptors;

import com.thena3ik.mealplanner.model.entity.UserEntity;

public interface TextInterceptor {
    boolean handle(UserEntity session, String text);
}
