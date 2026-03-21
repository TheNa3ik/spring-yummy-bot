package com.thena3ik.mealplanner.bot.handler.command;

import com.thena3ik.mealplanner.model.entity.UserEntity;
import com.thena3ik.mealplanner.model.enums.BotCommand;

public interface BotCommandHandler {

    BotCommand getSupportedCommand();

    void handle(UserEntity session, String text);
}
