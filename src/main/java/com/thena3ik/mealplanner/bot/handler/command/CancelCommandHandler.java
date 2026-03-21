package com.thena3ik.mealplanner.bot.handler.command;

import com.thena3ik.mealplanner.model.entity.UserEntity;
import com.thena3ik.mealplanner.model.enums.BotCommand;
import com.thena3ik.mealplanner.model.enums.UserState;
import com.thena3ik.mealplanner.service.LocaleService;
import com.thena3ik.mealplanner.service.TelegramService;
import com.thena3ik.mealplanner.service.UserService;
import org.springframework.stereotype.Component;

@Component
public class CancelCommandHandler implements BotCommandHandler {

    private final TelegramService telegramService;
    private final LocaleService localeService;
    private final UserService userService;

    public CancelCommandHandler(TelegramService telegramService, LocaleService localeService, UserService userService) {
        this.telegramService = telegramService;
        this.localeService = localeService;
        this.userService = userService;
    }

    @Override
    public BotCommand getSupportedCommand() {
        return BotCommand.CANCEL;
    }

    @Override
    public void handle(UserEntity session, String text) {
        String lang = session.getLanguageCode();

        if (session.getSearchState().getUserState() == UserState.ENTER_INGREDIENTS) {
            session.getSearchState().setUserState(UserState.MAIN_MENU);
            userService.save(session);

            telegramService.sendMainMenu(session, localeService.getMessage("start.welcome.back", lang, session.getFirstName()));
        }
    }
}
