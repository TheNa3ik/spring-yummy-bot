package com.thena3ik.yummybot.bot.handler.command;

import com.thena3ik.yummybot.model.entity.UserEntity;
import com.thena3ik.yummybot.model.enums.BotCommand;
import com.thena3ik.yummybot.model.enums.UserState;
import com.thena3ik.yummybot.service.LocaleService;
import com.thena3ik.yummybot.service.TelegramService;
import com.thena3ik.yummybot.service.UserService;
import com.thena3ik.yummybot.ui.ReplyKeyboardFactory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;

@Component
public class StartCommandHandler implements BotCommandHandler {

    private final TelegramService telegramService;
    private final LocaleService localeService;
    private final UserService userService;
    private final ReplyKeyboardFactory replyFactory;

    public StartCommandHandler(TelegramService telegramService,
                               LocaleService localeService,
                               UserService userService,
                               ReplyKeyboardFactory replyFactory) {
        this.telegramService = telegramService;
        this.localeService = localeService;
        this.userService = userService;
        this.replyFactory = replyFactory;
    }

    @Override
    public BotCommand getSupportedCommand() {
        return BotCommand.START;
    }

    @Override
    public void handle(UserEntity session, String text) {
        String lang = session.getLanguageCode();
        long chatId = session.getChatId();

        if (session.getDiet() == null) {
            session.setUserState(UserState.DIET_MENU);
            userService.save(session);

            String welcomeText = localeService.getMessage("start.welcome.text", lang, session.getFirstName());
            telegramService.sendMessage(chatId, welcomeText);

            String promptText = localeService.getMessage("start.welcome.new", lang);

            ReplyKeyboardMarkup keyboard = replyFactory.getDietMenuKeyboard(session);
            telegramService.sendMessage(chatId, promptText, keyboard);
        } else {
            session.setUserState(UserState.MAIN_MENU);
            userService.save(session);

            String welcomeBackText = localeService.getMessage("start.welcome.back", lang, session.getFirstName());

            ReplyKeyboardMarkup keyboard = replyFactory.getMainMenuKeyboard(session);
            telegramService.sendMessage(chatId, welcomeBackText, keyboard);
        }
    }
}
