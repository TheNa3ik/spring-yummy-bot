package com.thena3ik.mealplanner.bot.handler.state;

import com.thena3ik.mealplanner.model.entity.SearchStateEntity;
import com.thena3ik.mealplanner.model.entity.UserEntity;
import com.thena3ik.mealplanner.model.enums.Diet;
import com.thena3ik.mealplanner.model.enums.UserState;
import com.thena3ik.mealplanner.service.LocaleService;
import com.thena3ik.mealplanner.service.TelegramService;
import com.thena3ik.mealplanner.service.UserService;
import org.springframework.stereotype.Component;

@Component
public class DietMenuHandler implements StateHandler {

    private final TelegramService telegramService;
    private final LocaleService localeService;
    private final UserService userService;

    public DietMenuHandler(TelegramService telegramService, LocaleService localeService, UserService userService) {
        this.telegramService = telegramService;
        this.localeService = localeService;
        this.userService = userService;
    }

    @Override
    public UserState getSupportedState() {
        return UserState.DIET_MENU;
    }

    @Override
    public void handle(UserEntity session, String text) {
        String lang = session.getLanguageCode();
        long chatId = session.getChatId();

        if (text.equals(localeService.getMessage("btn.new", lang))) {
            session.getSearchState().setUserState(UserState.MAIN_MENU);
            userService.save(session);
            telegramService.sendMainMenu(session, localeService.getMessage("main.menu.text", lang));
            return;
        }

        if (text.equals(localeService.getMessage("btn.back", lang))) {
            session.getSearchState().setUserState(UserState.SETTINGS_MENU);
            userService.save(session);
            telegramService.sendSettingsMenu(session, localeService.getMessage("menu.settings.text", lang));
            return;
        }

        Diet selectedDiet = resolveDiet(text, lang);
        if (selectedDiet == null) {
            telegramService.sendMessage(chatId, localeService.getMessage("general.prompt.use_buttons", lang));
            return;
        }

        String dietCode = selectedDiet.getCode();

        session.setDiet(dietCode);

        SearchStateEntity searchState = session.getSearchState();
        searchState.setIngredients("");
        searchState.resetOffset();
        searchState.setRecipeId(0);

        session.getSearchState().setUserState(UserState.SETTINGS_MENU);
        userService.save(session);

        String displayDietName = localeService.getMessage(selectedDiet.getLabelText(), lang);
        telegramService.sendSettingsMenu(session, localeService.getMessage("menu.diet.confirm", lang, displayDietName));
    }

    private Diet resolveDiet(String text, String lang) {
        for (Diet diet : Diet.values()) {
            String translatedText = localeService.getMessage(diet.getLabelText(), lang);
            if (translatedText.equals(text)) {
                return diet;
            }
        }
        return null;
    }
}
