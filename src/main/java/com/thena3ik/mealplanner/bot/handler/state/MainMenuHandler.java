package com.thena3ik.mealplanner.bot.handler.state;

import com.thena3ik.mealplanner.model.entity.UserEntity;
import com.thena3ik.mealplanner.model.enums.UserState;
import com.thena3ik.mealplanner.service.LocaleService;
import com.thena3ik.mealplanner.service.TelegramService;
import org.springframework.stereotype.Component;

@Component
public class MainMenuHandler implements StateHandler {

    private final TelegramService telegramService;
    private final LocaleService localeService;

    public MainMenuHandler(TelegramService telegramService, LocaleService localeService) {
        this.telegramService = telegramService;
        this.localeService = localeService;
    }

    @Override
    public UserState getSupportedState() {
        return UserState.MAIN_MENU;
    }

    @Override
    public void handle(UserEntity session, String text) {
        String lang = session.getLanguageCode();

        telegramService.sendMainMenu(session, localeService.getMessage("general.prompt.use_buttons", lang));
    }
}
