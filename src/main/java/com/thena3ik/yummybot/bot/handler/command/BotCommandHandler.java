package com.thena3ik.yummybot.bot.handler.command;

import com.thena3ik.yummybot.model.entity.UserEntity;
import com.thena3ik.yummybot.model.enums.BotCommand;

public interface BotCommandHandler {

    BotCommand getSupportedCommand();

    void handle(UserEntity session, String text);
}
