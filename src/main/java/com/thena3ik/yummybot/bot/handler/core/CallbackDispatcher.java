package com.thena3ik.yummybot.bot.handler.core;

import com.thena3ik.yummybot.bot.handler.callback.CallbackHandler;
import com.thena3ik.yummybot.model.entity.UserEntity;
import com.thena3ik.yummybot.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

import java.util.List;

@Slf4j
@Component
public class CallbackDispatcher {

    private final TelegramService telegramService;
    private final LocaleService localeService;
    private final UserService userService;
    private final List<CallbackHandler> callbackHandlers;

    public CallbackDispatcher(TelegramService telegramService,
                              LocaleService localeService,
                              UserService userService,
                              List<CallbackHandler> callbackHandlers) {
        this.telegramService = telegramService;
        this.localeService = localeService;
        this.userService = userService;
        this.callbackHandlers = callbackHandlers;
    }

    public void handleCallback(CallbackQuery callback) {
        String data = callback.getData();
        UserEntity session = userService.findOrCreateById(callback.getMessage().getChatId());

        try {
            for (CallbackHandler handler : callbackHandlers) {
                if (data.startsWith(handler.getSupportedPrefix())) {
                    handler.handle(session, callback);
                    return;
                }
            }

            log.warn("No handler found for callback data: {}", data);
            telegramService.answerCallback(callback.getId(),
                    localeService.getMessage("general.error.unknown_action", session.getLanguageCode()));

        } catch (Exception e) {
            log.error("Failed to process callback query '{}' for user [ID: {}]", data, session.getChatId(), e);
            telegramService.answerCallback(callback.getId(),
                    localeService.getMessage("general.error.generic", session.getLanguageCode()));
        }
    }
}
