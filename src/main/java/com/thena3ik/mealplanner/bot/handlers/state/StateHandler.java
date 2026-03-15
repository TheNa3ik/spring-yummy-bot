package com.thena3ik.mealplanner.bot.handlers.state;

import com.thena3ik.mealplanner.models.user.UserSession;
import com.thena3ik.mealplanner.models.user.UserState;

public interface StateHandler {

    UserState getSupportedState();

    void handle(UserSession session, String text);
}
