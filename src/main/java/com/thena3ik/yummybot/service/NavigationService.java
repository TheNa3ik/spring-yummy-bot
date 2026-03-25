package com.thena3ik.yummybot.service;

import com.thena3ik.yummybot.model.entity.UserEntity;
import com.thena3ik.yummybot.model.enums.UserState;
import com.thena3ik.yummybot.ui.ReplyKeyboardFactory;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;

@Service
public class NavigationService {

    private final TelegramService telegramService;
    private final UserService userService;
    private final LocaleService localeService;
    private final ReplyKeyboardFactory replyFactory;

    public NavigationService(TelegramService telegramService,
                            UserService userService,
                            LocaleService localeService,
                            ReplyKeyboardFactory replyFactory) {
        this.telegramService = telegramService;
        this.userService = userService;
        this.localeService = localeService;
        this.replyFactory = replyFactory;
    }

    public boolean handleBackButtonToMain(UserEntity session, String text) {
        String lang = session.getLanguageCode();
        if (text.equals(localeService.getMessage("btn.back", lang))) {
            navigateToMainMenu(session);
            return true;
        }
        return false;
    }

    public boolean handleBackButtonToSettings(UserEntity session, String text) {
        String lang = session.getLanguageCode();
        if (text.equals(localeService.getMessage("btn.back", lang))) {
            navigateToSettingsMenu(session);
            return true;
        }
        return false;
    }

    public void navigateToMainMenu(UserEntity session) {
        session.setUserState(UserState.MAIN_MENU);
        userService.save(session);

        String lang = session.getLanguageCode();
        telegramService.sendMessage(
                session.getChatId(),
                localeService.getMessage("main.menu.text", lang),
                replyFactory.getMainMenuKeyboard(session)
        );
    }

    public void navigateToSettingsMenu(UserEntity session) {
        session.setUserState(UserState.SETTINGS_MENU);
        userService.save(session);

        String lang = session.getLanguageCode();
        telegramService.sendMessage(
                session.getChatId(),
                localeService.getMessage("menu.settings.text", lang),
                replyFactory.getSettingsMenuKeyboard(session)
        );
    }

    public void sendUnknownCommandError(UserEntity session, ReplyKeyboard fallbackKeyboard) {
        String lang = session.getLanguageCode();
        String errorText = localeService.getMessage("general.error.command.unknown", lang);
        telegramService.sendMessage(session.getChatId(), errorText, fallbackKeyboard);
    }
}