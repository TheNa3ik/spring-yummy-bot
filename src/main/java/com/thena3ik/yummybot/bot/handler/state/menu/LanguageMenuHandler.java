package com.thena3ik.yummybot.bot.handler.state.menu;

import com.thena3ik.yummybot.bot.handler.state.StateHandler;
import com.thena3ik.yummybot.model.entity.UserEntity;
import com.thena3ik.yummybot.model.enums.Language;
import com.thena3ik.yummybot.model.enums.UserState;
import com.thena3ik.yummybot.service.LocaleService;
import com.thena3ik.yummybot.service.NavigationService;
import com.thena3ik.yummybot.service.TelegramService;
import com.thena3ik.yummybot.service.UserService;
import com.thena3ik.yummybot.ui.ReplyKeyboardFactory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;

import java.util.Optional;

@Component
public class LanguageMenuHandler implements StateHandler {

    private final TelegramService telegramService;
    private final LocaleService localeService;
    private final UserService userService;
    private final NavigationService navigationService;
    private final ReplyKeyboardFactory replyFactory;

    public LanguageMenuHandler(TelegramService telegramService,
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
        return UserState.LANGUAGE_MENU;
    }

    @Override
    public void handle(UserEntity session, String text) {
        long chatId = session.getChatId();

        if (navigationService.handleBackButtonToSettings(session, text)) {
            return;
        }

        Optional<Language> selectedLangOpt = Language.fromDisplayText(text);
        if (selectedLangOpt.isEmpty()) {
            ReplyKeyboardMarkup fallbackKeyboard = replyFactory.getLanguageMenuKeyboard(session);
            navigationService.sendUnknownCommandError(session, fallbackKeyboard);
            return;
        }

        Language selectedLang = selectedLangOpt.get();
        session.setLanguageCode(selectedLang.getCode());
        session.setUserState(UserState.SETTINGS_MENU);
        userService.save(session);

        String successText = localeService.getMessage("menu.language.confirm", selectedLang.getCode());
        ReplyKeyboardMarkup keyboard = replyFactory.getSettingsMenuKeyboard(session);

        telegramService.sendMessage(chatId, successText, keyboard);
    }
}
