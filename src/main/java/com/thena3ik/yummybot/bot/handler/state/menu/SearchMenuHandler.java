package com.thena3ik.yummybot.bot.handler.state.menu;

import com.thena3ik.yummybot.bot.handler.state.StateHandler;
import com.thena3ik.yummybot.model.entity.SearchStateEntity;
import com.thena3ik.yummybot.model.entity.UserEntity;
import com.thena3ik.yummybot.model.enums.UserState;
import com.thena3ik.yummybot.service.LocaleService;
import com.thena3ik.yummybot.service.NavigationService;
import com.thena3ik.yummybot.service.TelegramService;
import com.thena3ik.yummybot.service.UserService;
import com.thena3ik.yummybot.ui.InlineKeyboardFactory;
import com.thena3ik.yummybot.ui.ReplyKeyboardFactory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;

@Component
public class SearchMenuHandler implements StateHandler {

    private final TelegramService telegramService;
    private final UserService userService;
    private final LocaleService localeService;
    private final NavigationService navigationService;
    private final ReplyKeyboardFactory replyFactory;
    private final InlineKeyboardFactory inlineFactory;

    public SearchMenuHandler(TelegramService telegramService,
                             UserService userService,
                             LocaleService localeService,
                             NavigationService navigationService,
                             ReplyKeyboardFactory replyFactory,
                             InlineKeyboardFactory inlineFactory) {
        this.telegramService = telegramService;
        this.userService = userService;
        this.localeService = localeService;
        this.navigationService = navigationService;
        this.replyFactory = replyFactory;
        this.inlineFactory = inlineFactory;
    }

    @Override
    public UserState getSupportedState() {
        return UserState.SEARCH_MENU;
    }

    @Override
    public void handle(UserEntity session, String text) {
        String lang = session.getLanguageCode();

        if (text.equals(localeService.getMessage("menu.search.btn.ingredients", lang))) {
            startSearchFlow(session, UserState.CUISINE_FOR_INGREDIENT, lang);
            return;
        }

        if (text.equals(localeService.getMessage("menu.search.btn.name", lang))) {
            startSearchFlow(session, UserState.CUISINE_FOR_NAME, lang);
            return;
        }

        if (text.equals(localeService.getMessage("menu.search.btn.random", lang))) {
            startSearchFlow(session, UserState.CUISINE_FOR_RANDOM, lang);
            return;
        }

        if (navigationService.handleBackButtonToMain(session, text)) {
            return;
        }

        ReplyKeyboardMarkup fallbackKeyboard = replyFactory.getSearchMenuKeyboard(session);
        navigationService.sendUnknownCommandError(session, fallbackKeyboard);
    }

    private void startSearchFlow(UserEntity session, UserState targetState, String lang) {
        long chatId = session.getChatId();

        if (session.getSearchState() == null) {
            session.setSearchState(new SearchStateEntity());
        }
        if (session.getDiet() == null) {
            session.setDiet("none");
        }

        SearchStateEntity searchState = session.getSearchState();
        searchState.resetOffset();
        searchState.setIngredients("");
        searchState.setCuisine(null);

        session.setUserState(targetState);
        userService.save(session);

        String cuisineText = localeService.getMessage("menu.cuisine.text", lang);

        InlineKeyboardMarkup keyboard = inlineFactory.getCuisineKeyboard(session, 1);
        telegramService.sendMessage(chatId, cuisineText, keyboard);
    }
}
