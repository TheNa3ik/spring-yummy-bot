package com.thena3ik.mealplanner.bot.handler.state;

import com.thena3ik.mealplanner.model.entity.UserEntity;
import com.thena3ik.mealplanner.model.enums.UserState;

public interface StateHandler {

    UserState getSupportedState();

    void handle(UserEntity session, String text);
}
