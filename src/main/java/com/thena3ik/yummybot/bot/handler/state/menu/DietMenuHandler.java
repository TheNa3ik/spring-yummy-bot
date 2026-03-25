package com.thena3ik.yummybot.bot.handler.state.menu;

import com.thena3ik.yummybot.bot.handler.state.StateHandler;
import com.thena3ik.yummybot.model.entity.SearchStateEntity;
import com.thena3ik.yummybot.model.entity.UserEntity;
import com.thena3ik.yummybot.model.enums.Diet;
import com.thena3ik.yummybot.model.enums.UserState;
import com.thena3ik.yummybot.service.LocaleService;
import com.thena3ik.yummybot.service.NavigationService;
import com.thena3ik.yummybot.service.TelegramService;
import com.thena3ik.yummybot.service.UserService;
import com.thena3ik.yummybot.ui.ReplyKeyboardFactory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;

@Component
public class DietMenuHandler implements StateHandler {

    private final TelegramService telegramService;
    private final LocaleService localeService;
    private final UserService userService;
    private final NavigationService navigationService;
    private final ReplyKeyboardFactory replyFactory;

    public DietMenuHandler(TelegramService telegramService,
                           LocaleService localeService,
                           UserService userService,
                           NavigationService navigationService,
                           ReplyKeyboardFactory replyFactory) {
        this.telegramService = telegramService;
        this.localeService = localeService;
        this.userService = userService;
        this.navigationService = navigationService;
        this.replyFactory = replyFactory;
    }

    @Override
    public UserState getSupportedState() {
        return UserState.DIET_MENU;
    }

    @Override
    public void handle(UserEntity session, String text) {
        String lang = session.getLanguageCode();
        boolean isNewUser = session.getDiet() == null;

        if (isNewUser && text.equals(localeService.getMessage("btn.new", lang))) {
            navigationService.navigateToMainMenu(session);
            return;
        }

        if (!isNewUser && navigationService.handleBackButtonToSettings(session, text)) {
            return;
        }

        Diet selectedDiet = resolveDiet(text, lang);
        if (selectedDiet == null) {
            ReplyKeyboardMarkup fallbackKeyboard = replyFactory.getDietMenuKeyboard(session);
            navigationService.sendUnknownCommandError(session, fallbackKeyboard);
            return;
        }

        session.setDiet(selectedDiet.getCode());

        SearchStateEntity searchState = session.getSearchState();
        if (searchState != null) {
            searchState.setIngredients("");
            searchState.resetOffset();
            searchState.setRecipeId(0);
        }

        String displayDietName = localeService.getMessage(selectedDiet.getLabelText(), lang);
        String confirmText = localeService.getMessage("menu.diet.confirm", lang, displayDietName);

        if (isNewUser) {
            session.setUserState(UserState.MAIN_MENU);
            userService.save(session);
            telegramService.sendMessage(session.getChatId(), confirmText, replyFactory.getMainMenuKeyboard(session));
        } else {
            session.setUserState(UserState.SETTINGS_MENU);
            userService.save(session);
            telegramService.sendMessage(session.getChatId(), confirmText, replyFactory.getSettingsMenuKeyboard(session));
        }
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