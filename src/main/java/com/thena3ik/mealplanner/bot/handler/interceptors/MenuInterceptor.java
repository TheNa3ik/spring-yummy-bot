package com.thena3ik.mealplanner.bot.handler.interceptors;

import com.thena3ik.mealplanner.model.entity.SearchStateEntity;
import com.thena3ik.mealplanner.model.entity.UserEntity;
import com.thena3ik.mealplanner.model.enums.UserState;
import com.thena3ik.mealplanner.service.LocaleService;
import com.thena3ik.mealplanner.service.TelegramService;
import com.thena3ik.mealplanner.service.UserService;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@Order(2)
public class MenuInterceptor implements TextInterceptor {

    private final TelegramService telegramService;
    private final UserService userService;
    private final LocaleService localeService;

    public MenuInterceptor(TelegramService telegramService, UserService userService, LocaleService localeService) {
        this.telegramService = telegramService;
        this.userService = userService;
        this.localeService = localeService;
    }

    @Override
    public boolean handle(UserEntity session, String text) {
        String lang = session.getLanguageCode();

        if (text.equals(localeService.getMessage("main.menu.btn.search", lang))) {
            handleSearch(session, lang);
            return true;
        }

        if (text.equals(localeService.getMessage("main.menu.btn.settings", lang))) {
            session.getSearchState().setUserState(UserState.SETTINGS_MENU);
            userService.save(session);
            telegramService.sendSettingsMenu(session, localeService.getMessage("menu.settings.text", lang));
            return true;
        }

        if (text.equals(localeService.getMessage("main.menu.btn.about", lang))) {
            telegramService.sendAboutMeMessage(session, localeService.getMessage("about.text", lang));
            return true;
        }

        return false;
    }

    private void handleSearch (UserEntity session, String lang) {
        SearchStateEntity searchState = session.getSearchState();

        if (session.getSearchState() == null) {
            session.setSearchState(new SearchStateEntity());
        }
        if (session.getDiet() == null) {
            session.setDiet("none");
        }

        Objects.requireNonNull(searchState).resetOffset();
        searchState.setIngredients("");

        session.getSearchState().setUserState(UserState.ENTER_INGREDIENTS);
        userService.save(session);
        telegramService.sendMessage(session.getChatId(), localeService.getMessage("flow.search.prompt.ingredients", lang));
    }
}
