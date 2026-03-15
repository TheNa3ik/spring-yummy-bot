package com.thena3ik.mealplanner.bot.handlers.state;

import com.thena3ik.mealplanner.models.enums.Language;
import com.thena3ik.mealplanner.models.user.UserSession;
import com.thena3ik.mealplanner.models.user.UserState;
import com.thena3ik.mealplanner.service.LocaleService;
import com.thena3ik.mealplanner.service.TelegramService;
import com.thena3ik.mealplanner.service.UserService;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ChooseLanguageHandler implements StateHandler {

    private final TelegramService telegramService;
    private final LocaleService localeService;
    private final UserService userService;

    public ChooseLanguageHandler (TelegramService telegramService, LocaleService localeService, UserService userService) {
        this.telegramService = telegramService;
        this.localeService = localeService;
        this.userService = userService;
    }

    @Override
    public UserState getSupportedState() {
        return UserState.CHOOSE_LANGUAGE;
    }

    @Override
    public void handle(UserSession session, String text) {
        Optional<Language> selectedLangOpt = Language.fromDisplayText(text);

        String currentLang = session.getLanguageCode();

        if (selectedLangOpt.isEmpty()) {
            telegramService.sendMessage(session.getChatId(), localeService.getMessage("error.invalid", currentLang));
            return;
        }

        Language selectedLang = selectedLangOpt.get();
        session.setLanguageCode(selectedLang.getCode());
        session.setUserState(UserState.IDLE);
        userService.save(session);

        String newLangCode = selectedLang.getCode();

        String confirmationText = localeService.getMessage("language.changed", newLangCode);
        String menuText = localeService.getMessage("menu.idle", newLangCode);

        telegramService.sendMainMenu(session.getChatId(), confirmationText + "\n\n" + menuText, newLangCode);
    }
}
