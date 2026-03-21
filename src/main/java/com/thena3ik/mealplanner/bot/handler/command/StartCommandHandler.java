package com.thena3ik.mealplanner.bot.handler.command;

import com.thena3ik.mealplanner.model.entity.UserEntity;
import com.thena3ik.mealplanner.model.enums.BotCommand;
import com.thena3ik.mealplanner.model.enums.UserState;
import com.thena3ik.mealplanner.service.LocaleService;
import com.thena3ik.mealplanner.service.TelegramService;
import com.thena3ik.mealplanner.service.UserService;
import org.springframework.stereotype.Component;

@Component
public class StartCommandHandler implements BotCommandHandler {

    private final TelegramService telegramService;
    private final LocaleService localeService;
    private final UserService userService;

    public StartCommandHandler(TelegramService telegramService, LocaleService localeService, UserService userService) {
        this.telegramService = telegramService;
        this.localeService = localeService;
        this.userService = userService;
    }

    @Override
    public BotCommand getSupportedCommand() {
        return BotCommand.START;
    }

    @Override
    public void handle(UserEntity session, String text) {
        String lang = session.getLanguageCode();

        if (session.getDiet() == null) {
            session.getSearchState().setUserState(UserState.DIET_MENU);
            userService.save(session);

            telegramService.sendMessage(session.getChatId(), localeService.getMessage("start.welcome.text", lang, session.getFirstName()));
            telegramService.sendDietKeyboard(session,
                    localeService.getMessage("start.welcome.new", lang));
        } else {
            session.getSearchState().setUserState(UserState.MAIN_MENU);
            userService.save(session);

            telegramService.sendMainMenu(session,
                    localeService.getMessage("start.welcome.back", lang, session.getFirstName()));
        }
    }
}
