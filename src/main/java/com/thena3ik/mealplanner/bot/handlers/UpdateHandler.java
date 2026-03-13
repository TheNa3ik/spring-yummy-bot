package com.thena3ik.mealplanner.bot.handlers;

import com.thena3ik.mealplanner.bot.handlers.state.StateHandler;
import com.thena3ik.mealplanner.models.*;
import com.thena3ik.mealplanner.models.user.UserSession;
import com.thena3ik.mealplanner.models.user.UserState;
import com.thena3ik.mealplanner.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class UpdateHandler {

    private final UserService userService;
    private final CallbackQueryHandler callbackQueryHandler;
    private final CommandDispatcher commandDispatcher;
    private final Map<UserState, StateHandler> stateHandlers;

    @Autowired
    public UpdateHandler(UserService userService,
                         CallbackQueryHandler callbackQueryHandler,
                         CommandDispatcher commandDispatcher,
                         List<StateHandler> handlers) {
        this.userService = userService;
        this.callbackQueryHandler = callbackQueryHandler;
        this.commandDispatcher = commandDispatcher;

        this.stateHandlers = handlers.stream()
                .collect(Collectors.toMap(StateHandler::getSupportedState, Function.identity()));
    }

    public void handle(Update update) {
        if (update.hasCallbackQuery()) {
            callbackQueryHandler.handleCallback(update.getCallbackQuery());
            return;
        }

        if (!update.hasMessage() || !update.getMessage().hasText()) return;

        long chatId = update.getMessage().getChatId();
        String text = update.getMessage().getText();
        UserSession session = userService.findOrCreateById(chatId);

        if (session.getLanguageCode() == null) {
            String telegramLang = update.getMessage().getFrom().getLanguageCode();
            session.setLanguageCode(telegramLang);
            userService.save(session);
        }

        session.setFirstName(update.getMessage().getFrom().getFirstName());
        UserState state = session.getUserState();

        if (commandDispatcher.processIfCommand(session, text)) {
            return;
        }

        StateHandler handler = stateHandlers.getOrDefault(session.getUserState(), stateHandlers.get(UserState.IDLE));
        handler.handle(session, text);
    }
}
