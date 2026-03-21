package com.thena3ik.mealplanner.bot.handler.state;

import com.thena3ik.mealplanner.model.entity.UserEntity;
import com.thena3ik.mealplanner.model.enums.Language;
import com.thena3ik.mealplanner.model.enums.UserState;
import com.thena3ik.mealplanner.service.LocaleService;
import com.thena3ik.mealplanner.service.TelegramService;
import com.thena3ik.mealplanner.service.UserService;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class LanguageMenuHandler implements StateHandler {

    private final TelegramService telegramService;
    private final LocaleService localeService;
    private final UserService userService;

    public LanguageMenuHandler(TelegramService telegramService, LocaleService localeService, UserService userService) {
        this.telegramService = telegramService;
        this.localeService = localeService;
        this.userService = userService;
    }

    @Override
    public UserState getSupportedState() {
        return UserState.LANGUAGE_MENU;
    }

    @Override
    public void handle(UserEntity session, String text) {
        Optional<Language> selectedLangOpt = Language.fromDisplayText(text);

        long chatId = session.getChatId();
        String currentLang = session.getLanguageCode();

        if (text.equals(localeService.getMessage("btn.back", currentLang))) {
            session.getSearchState().setUserState(UserState.SETTINGS_MENU);
            userService.save(session);
            telegramService.sendSettingsMenu(session, localeService.getMessage("menu.settings.text", currentLang));
            return;
        }

        if (selectedLangOpt.isEmpty()) {
            telegramService.sendMessage(chatId, localeService.getMessage("general.prompt.use_buttons", currentLang));
            return;
        }

        Language selectedLang = selectedLangOpt.get();
        session.setLanguageCode(selectedLang.getCode());
        session.getSearchState().setUserState(UserState.SETTINGS_MENU);
        userService.save(session);

        String newLangCode = selectedLang.getCode();
        telegramService.sendSettingsMenu(session, localeService.getMessage("menu.language.confirm", newLangCode));
    }
}
