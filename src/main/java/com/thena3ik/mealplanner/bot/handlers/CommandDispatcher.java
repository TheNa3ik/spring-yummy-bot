package com.thena3ik.mealplanner.bot.handlers;

import com.thena3ik.mealplanner.bot.commands.BotCommand;
import com.thena3ik.mealplanner.models.LastSearch;
import com.thena3ik.mealplanner.models.user.UserSession;
import com.thena3ik.mealplanner.models.user.UserState;
import com.thena3ik.mealplanner.service.LocaleService;
import com.thena3ik.mealplanner.service.TelegramService;
import com.thena3ik.mealplanner.service.UserService;
import org.springframework.stereotype.Component;

@Component
public class CommandDispatcher {

    private final TelegramService telegramService;
    private final LocaleService localeService;
    private final UserService userService;

    public CommandDispatcher (TelegramService telegramService, LocaleService localeService, UserService userService) {
        this.telegramService = telegramService;
        this.localeService = localeService;
        this.userService = userService;
    }

    public boolean processIfCommand(UserSession session, String text) {
        String lang = session.getLanguageCode();
        BotCommand command = resolveCommand(text, lang);

        if (command == null) {
            return false;
        }

        executeCommand(session, command, lang);
        return true;
    }

    private BotCommand resolveCommand(String text, String langCode) {
        BotCommand command = BotCommand.fromText(text);
        if (command != null) return command;

        if (text.equals(localeService.getMessage("menu.button.search", langCode))) {
            return BotCommand.SEARCH;
        }
        if (text.equals(localeService.getMessage("menu.button.diet", langCode))) {
            return BotCommand.DIET_PREFERENCES;
        }
        if (text.equals(localeService.getMessage("menu.button.language", langCode))) {
            return BotCommand.LANGUAGE;
        }

        return null;
    }

    public void executeCommand(UserSession session, BotCommand command, String lang) {
        long chatId = session.getChatId();

        switch (command) {
            case START -> handleStart(session);
            case SEARCH -> handleSearch(session);
            case DIET_PREFERENCES -> {
                session.setUserState(UserState.CHOOSE_DIET);
                userService.save(session);
                telegramService.sendDietKeyboard(chatId, localeService.getMessage("diet.choose", lang), lang);
            }
            case LANGUAGE -> {
                session.setUserState(UserState.CHOOSE_LANGUAGE);
                userService.save(session);
                telegramService.sendLanguageMenu(chatId, localeService.getMessage("language.choose", lang));
            }
            default -> telegramService.sendMainMenu(chatId, localeService.getMessage("error.command.unknown", lang), lang);
        }
    }

    private void handleStart(UserSession session) {
        String lang = session.getLanguageCode();
        if (!session.hasDiet()) {
            session.setUserState(UserState.CHOOSE_DIET);
            telegramService.sendDietKeyboard(session.getChatId(),
                    localeService.getMessage("welcome.new", lang, session.getFirstName()), lang);
        } else {
            telegramService.sendMainMenu(session.getChatId(),
                    localeService.getMessage("welcome.back", lang, session.getFirstName()), lang);
        }
    }

    private void handleSearch (UserSession session) {
        String lang = session.getLanguageCode();
        LastSearch search = session.getLastSearch();

        if (search == null) {
            search = new LastSearch("none", "", 0);
            session.setLastSearch(search);
        }

        search.resetOffset();
        search.setIngredients("");

        session.setUserState(UserState.ENTER_INGREDIENTS);
        userService.save(session);
        telegramService.sendMessage(session.getChatId(), localeService.getMessage("search.start", lang));
    }
}
