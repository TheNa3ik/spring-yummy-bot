package com.thena3ik.yummybot.bot.handler.state.menu;

import com.thena3ik.yummybot.bot.handler.state.StateHandler;
import com.thena3ik.yummybot.model.entity.SearchStateEntity;
import com.thena3ik.yummybot.model.entity.UserEntity;
import com.thena3ik.yummybot.model.enums.UserState;
import com.thena3ik.yummybot.service.LocaleService;
import com.thena3ik.yummybot.service.NavigationService;
import com.thena3ik.yummybot.ui.ReplyKeyboardFactory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;

@Component
public class RecipeMenuHandler implements StateHandler {

    private final LocaleService localeService;
    private final NavigationService navigationService;
    private final ReplyKeyboardFactory replyFactory;

    public RecipeMenuHandler(LocaleService localeService,
                             NavigationService navigationService,
                             ReplyKeyboardFactory replyFactory) {
        this.localeService = localeService;
        this.navigationService = navigationService;
        this.replyFactory = replyFactory;
    }

    @Override
    public UserState getSupportedState() {
        return UserState.SHOW_RECIPES;
    }

    @Override
    public void handle(UserEntity session, String text) {
        String lang = session.getLanguageCode();

        if (text.equals(localeService.getMessage("btn.back", lang))) {

            SearchStateEntity searchState = session.getSearchState();
            if (searchState != null) {
                searchState.setSearchName("");
                searchState.setIngredients("");
                searchState.setCuisine(null);
                searchState.resetOffset();
            }

            navigationService.navigateToMainMenu(session);
            return;
        }

        ReplyKeyboardMarkup fallbackKeyboard = replyFactory.getRecipeMenuKeyboard(session);
        navigationService.sendUnknownCommandError(session, fallbackKeyboard);
    }
}