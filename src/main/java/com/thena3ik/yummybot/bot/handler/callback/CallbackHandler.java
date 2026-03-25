package com.thena3ik.yummybot.bot.handler.callback;

import com.thena3ik.yummybot.model.entity.UserEntity;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

public interface CallbackHandler {

    String getSupportedPrefix();

    void handle(UserEntity session, CallbackQuery callback);
}
