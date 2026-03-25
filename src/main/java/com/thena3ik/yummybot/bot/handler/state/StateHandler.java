package com.thena3ik.yummybot.bot.handler.state;

import com.thena3ik.yummybot.model.entity.UserEntity;
import com.thena3ik.yummybot.model.enums.UserState;

public interface StateHandler {

    UserState getSupportedState();

    void handle(UserEntity session, String text);
}
