package com.thena3ik.yummybot.bot.handler.callback;

import com.thena3ik.yummybot.model.entity.SearchStateEntity;
import com.thena3ik.yummybot.model.entity.UserEntity;
import com.thena3ik.yummybot.service.NavigationService;
import com.thena3ik.yummybot.service.TelegramService;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

@Component
public class CancelCallbackHandler implements CallbackHandler {

    private final TelegramService telegramService;
    private final NavigationService navigationService;

    public CancelCallbackHandler(TelegramService telegramService,
                                 NavigationService navigationService) {
        this.telegramService = telegramService;
        this.navigationService = navigationService;
    }

    @Override
    public String getSupportedPrefix() {
        return "cancel";
    }

    @Override
    public void handle(UserEntity session, CallbackQuery callback) {
        int messageId = callback.getMessage().getMessageId();
        String callbackId = callback.getId();
        long chatId = session.getChatId();

        if (callback.getData().equals("cancel_recipe")) {
            telegramService.answerCallback(callbackId, null);
            telegramService.deleteMessage(chatId, messageId);
            return;
        }

        SearchStateEntity searchState = session.getSearchState();
        if (searchState != null) {
            searchState.setIngredients("");
            searchState.setSearchName("");
            searchState.setCuisine(null);
            searchState.resetOffset();
        }

        telegramService.answerCallback(callbackId, null);
        telegramService.deleteMessage(chatId, messageId);

        navigationService.navigateToMainMenu(session);
    }
}