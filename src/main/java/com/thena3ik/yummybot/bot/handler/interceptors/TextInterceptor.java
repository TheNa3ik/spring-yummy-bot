package com.thena3ik.yummybot.bot.handler.interceptors;

import com.thena3ik.yummybot.model.entity.UserEntity;

public interface TextInterceptor {
    boolean handle(UserEntity session, String text);
}
