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
public class SettingsMenuHandler implements StateHandler {

    private final TelegramService telegramService;
    private final LocaleService localeService;
    private final UserService userService;
    private final NavigationService navigationService;
    private final ReplyKeyboardFactory replyFactory;
    private final InlineKeyboardFactory inlineFactory;

    public SettingsMenuHandler(TelegramService telegramService,
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
        return UserState.SETTINGS_MENU;
    }

    @Override
    public void handle(UserEntity session, String text) {
        String lang = session.getLanguageCode();
        long chatId = session.getChatId();

        boolean isCurrentlyAiEnabled = session.isAiTranslationEnabled();
        String aiButton = isCurrentlyAiEnabled ? "menu.settings.btn.ai_on" : "menu.settings.btn.ai_off";

        if (text.equals(localeService.getMessage(aiButton, lang))) {
            session.setAiTranslationEnabled(!session.isAiTranslationEnabled());
            userService.save(session);

            String successTextKey = isCurrentlyAiEnabled ? "menu.settings.ai_off.confirmation" : "menu.settings.ai_on.confirmation";
            String successText = localeService.getMessage(successTextKey, lang);

            ReplyKeyboardMarkup keyboard = replyFactory.getSettingsMenuKeyboard(session);
            telegramService.sendMessage(chatId, successText, keyboard);
            return;
        }

        if (text.equals(localeService.getMessage("menu.settings.btn.diet", lang))) {
            session.setUserState(UserState.DIET_MENU);
            userService.save(session);

            String dietText = localeService.getMessage("menu.diet.text", lang);

            ReplyKeyboardMarkup keyboard = replyFactory.getDietMenuKeyboard(session);
            telegramService.sendMessage(chatId, dietText, keyboard);
            return;
        }

        if (text.equals(localeService.getMessage("menu.settings.btn.language", lang))) {
            session.setUserState(UserState.LANGUAGE_MENU);
            userService.save(session);

            String langText = localeService.getMessage("menu.language.text", lang);

            ReplyKeyboardMarkup keyboard = replyFactory.getLanguageMenuKeyboard(session);
            telegramService.sendMessage(chatId, langText, keyboard);
            return;
        }

        if (text.equals(localeService.getMessage("menu.settings.btn.intolerances", lang))) {
            String intolText = localeService.getMessage("menu.intolerances.text", lang);

            InlineKeyboardMarkup keyboard = inlineFactory.getIntoleranceKeyboard(session);
            telegramService.sendMessage(chatId, intolText, keyboard);
            return;
        }

        if (navigationService.handleBackButtonToMain(session, text)) {
            return;
        }

        ReplyKeyboardMarkup fallbackKeyboard = replyFactory.getSettingsMenuKeyboard(session);
        navigationService.sendUnknownCommandError(session, fallbackKeyboard);
    }
}
