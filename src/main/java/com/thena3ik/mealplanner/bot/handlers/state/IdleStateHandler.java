package com.thena3ik.mealplanner.bot.handlers.state;

import com.thena3ik.mealplanner.models.user.UserSession;
import com.thena3ik.mealplanner.models.user.UserState;
import com.thena3ik.mealplanner.service.LocaleService;
import com.thena3ik.mealplanner.service.TelegramService;
import org.springframework.stereotype.Component;

@Component
public class IdleStateHandler implements StateHandler {

    private final TelegramService telegramService;
    private final LocaleService localeService;

    public IdleStateHandler (TelegramService telegramService, LocaleService localeService) {
        this.telegramService = telegramService;
        this.localeService = localeService;
    }

    @Override
    public UserState getSupportedState() {
        return UserState.IDLE;
    }

    @Override
    public void handle(UserSession session, String text) {
        String lang = session.getLanguageCode();
        telegramService.sendMainMenu(session.getChatId(), localeService.getMessage("menu.idle", lang), lang);
    }
}
