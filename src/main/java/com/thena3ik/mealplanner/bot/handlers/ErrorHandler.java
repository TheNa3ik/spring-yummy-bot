package com.thena3ik.mealplanner.bot.handlers;

import com.thena3ik.mealplanner.service.LocaleService;
import com.thena3ik.mealplanner.service.TelegramService;
import com.thena3ik.mealplanner.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

@Slf4j
@Component
public class ErrorHandler {

    private final TelegramService telegramService;
    private final UserService userService;
    private final LocaleService localeService;

    public ErrorHandler(TelegramService telegramService, UserService userService, LocaleService localeService) {
        this.telegramService = telegramService;
        this.userService = userService;
        this.localeService = localeService;
    }

    public void handle(Exception e, Update update) {
        log.error("Failed to process Telegram update [ID: {}]", update.getUpdateId(), e);

        try {
            long chatId = 0L;
            String callbackId = null;
            String lang = "en";

            if (update.hasMessage() && update.getMessage().hasText()) {
                chatId = update.getMessage().getChatId();
            } else if (update.hasCallbackQuery()) {
                chatId = update.getCallbackQuery().getMessage().getChatId();
                callbackId = update.getCallbackQuery().getId();
            }

            if (chatId != 0L) {
                try {
                    lang = userService.findOrCreateById(chatId).getLanguageCode();
                } catch (Exception dbException) {
                    log.warn("Could not fetch user language. Using English as Default.", dbException);
                }

                String errorMessage = localeService.getMessage("error.generic", lang);

                if (callbackId != null) {
                    telegramService.answerCallback(callbackId, errorMessage);
                } else {
                    telegramService.sendMessage(chatId, errorMessage);
                }
            }
        } catch (Exception fatal) {
            log.error("Critical failure during error fallback execution for update [ID: {}]", update.getUpdateId(), fatal);
        }
    }
}
