package com.thena3ik.yummybot.bot.handler.state.menu;

import com.thena3ik.yummybot.bot.handler.state.StateHandler;
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
public class MainMenuHandler implements StateHandler {

    private final TelegramService telegramService;
    private final LocaleService localeService;
    private final UserService userService;
    private final NavigationService navigationService;
    private final ReplyKeyboardFactory replyFactory;
    private final InlineKeyboardFactory inlineFactory;

    public MainMenuHandler(TelegramService telegramService,
                           LocaleService localeService,
                           UserService userService,
                           NavigationService navigationService,
                           ReplyKeyboardFactory replyFactory,
                           InlineKeyboardFactory inlineFactory) {
        this.telegramService = telegramService;
        this.localeService = localeService;
        this.userService = userService;
        this.navigationService = navigationService;
        this.replyFactory = replyFactory;
        this.inlineFactory = inlineFactory;
    }

    @Override
    public UserState getSupportedState() {
        return UserState.MAIN_MENU;
    }

    @Override
    public void handle(UserEntity session, String text) {
        String lang = session.getLanguageCode();
        long chatId = session.getChatId();

        if (text.equals(localeService.getMessage("main.menu.btn.search", lang))) {
            session.setUserState(UserState.SEARCH_MENU);
            userService.save(session);

            String searchText = localeService.getMessage("menu.search.text", lang);
            ReplyKeyboardMarkup keyboard = replyFactory.getSearchMenuKeyboard(session);

            telegramService.sendMessage(chatId, searchText, keyboard);
            return;
        }

        if (text.equals(localeService.getMessage("main.menu.btn.settings", lang))) {
            navigationService.navigateToSettingsMenu(session);
            return;
        }

        if (text.equals(localeService.getMessage("main.menu.btn.about", lang))) {
            String aboutText = localeService.getMessage("about.text", lang);
            InlineKeyboardMarkup keyboard = inlineFactory.getAboutKeyboard();

            telegramService.sendMessage(chatId, aboutText, keyboard);
            return;
        }

        ReplyKeyboardMarkup fallbackKeyboard = replyFactory.getMainMenuKeyboard(session);
        navigationService.sendUnknownCommandError(session, fallbackKeyboard);
    }
}