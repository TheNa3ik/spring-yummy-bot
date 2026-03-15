package com.thena3ik.mealplanner.bot.handlers.state;

import com.thena3ik.mealplanner.models.enums.Diet;
import com.thena3ik.mealplanner.models.user.LastSearch;
import com.thena3ik.mealplanner.models.user.UserSession;
import com.thena3ik.mealplanner.models.user.UserState;
import com.thena3ik.mealplanner.service.LocaleService;
import com.thena3ik.mealplanner.service.TelegramService;
import com.thena3ik.mealplanner.service.UserService;
import org.springframework.stereotype.Component;

@Component
public class ChooseDietHandler implements StateHandler {

    private final TelegramService telegramService;
    private final LocaleService localeService;
    private final UserService userService;

    public ChooseDietHandler (TelegramService telegramService, LocaleService localeService, UserService userService) {
        this.telegramService = telegramService;
        this.localeService = localeService;
        this.userService = userService;
    }

    @Override
    public UserState getSupportedState() {
        return UserState.CHOOSE_DIET;
    }

    @Override
    public void handle(UserSession session, String text) {
        String lang = session.getLanguageCode();

        Diet selectedDiet = resolveDiet(text, lang);

        if (selectedDiet == null) {
            telegramService.sendMessage(session.getChatId(), localeService.getMessage("error.invalid", lang));
            return;
        }

        String dietApiValue = selectedDiet.getApiValue();

        session.setLastSearch(new LastSearch(dietApiValue, "", 0));
        session.setUserState(UserState.IDLE);
        userService.save(session);

        String displayDietName = localeService.getMessage(selectedDiet.getLabelKey(), lang);
        telegramService.sendMainMenu(session.getChatId(),
                localeService.getMessage("diet.confirm", lang, displayDietName), lang);
    }

    private Diet resolveDiet(String text, String lang) {
        for (Diet diet : Diet.values()) {
            String translatedText = localeService.getMessage(diet.getLabelKey(), lang);
            if (translatedText.equals(text)) {
                return diet;
            }
        }
        return null;
    }
}
