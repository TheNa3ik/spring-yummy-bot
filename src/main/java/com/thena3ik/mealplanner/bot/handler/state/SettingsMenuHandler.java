package com.thena3ik.mealplanner.bot.handler.state;

import com.thena3ik.mealplanner.model.entity.UserEntity;
import com.thena3ik.mealplanner.model.enums.UserState;
import com.thena3ik.mealplanner.service.LocaleService;
import com.thena3ik.mealplanner.service.TelegramService;
import com.thena3ik.mealplanner.service.UserService;
import org.springframework.stereotype.Component;

@Component
public class SettingsMenuHandler implements StateHandler {

    private final TelegramService telegramService;
    private final LocaleService localeService;
    private final UserService userService;

    public SettingsMenuHandler(TelegramService telegramService, LocaleService localeService, UserService userService) {
        this.telegramService = telegramService;
        this.localeService = localeService;
        this.userService = userService;
    }

    @Override
    public UserState getSupportedState() {
        return UserState.SETTINGS_MENU;
    }

    @Override
    public void handle(UserEntity session, String text) {
        String lang = session.getLanguageCode();
        boolean isAiTranslationEnabled = session.isAiTranslationEnabled();

        String aiButton = isAiTranslationEnabled ? "menu.settings.btn.ai_on" : "menu.settings.btn.ai_off";
        if (text.equals(localeService.getMessage(aiButton, lang))) {
            session.setAiTranslationEnabled(!session.isAiTranslationEnabled());
            userService.save(session);

            String successText = isAiTranslationEnabled ? "menu.settings.ai_off.confirmation" : "menu.settings.ai_on.confirmation";

            telegramService.sendSettingsMenu(session, localeService.getMessage(successText, lang));
            return;
        }

        if (text.equals(localeService.getMessage("menu.settings.btn.diet", lang))) {
            session.getSearchState().setUserState(UserState.DIET_MENU);
            userService.save(session);
            telegramService.sendDietKeyboard(session, localeService.getMessage("menu.diet.text", lang));
            return;
        }

        if (text.equals(localeService.getMessage("menu.settings.btn.language", lang))) {
            session.getSearchState().setUserState(UserState.LANGUAGE_MENU);
            userService.save(session);
            telegramService.sendLanguageMenu(session, localeService.getMessage("menu.language.text", lang));
            return;
        }

        if (text.equals(localeService.getMessage("btn.back", lang))) {
            session.getSearchState().setUserState(UserState.MAIN_MENU);
            userService.save(session);
            telegramService.sendMainMenu(session, localeService.getMessage("main.menu.text", lang));
            return;
        }

        telegramService.sendSettingsMenu(session, localeService.getMessage("general.error.command.unknown", lang));
    }
}
