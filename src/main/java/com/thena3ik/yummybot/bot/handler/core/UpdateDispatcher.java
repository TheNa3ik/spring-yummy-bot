package com.thena3ik.yummybot.bot.handler.core;

import com.thena3ik.yummybot.bot.handler.interceptors.TextInterceptor;
import com.thena3ik.yummybot.bot.handler.state.StateHandler;
import com.thena3ik.yummybot.model.entity.UserEntity;
import com.thena3ik.yummybot.model.enums.UserState;
import com.thena3ik.yummybot.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class UpdateDispatcher {

    private final UserService userService;
    private final CallbackDispatcher callbackDispatcher;
    private final List<TextInterceptor> interceptors;
    private final Map<UserState, StateHandler> stateHandlers;

    @Autowired
    public UpdateDispatcher(UserService userService,
                            CallbackDispatcher callbackDispatcher,
                            List<TextInterceptor> interceptors,
                            List<StateHandler> stateHandlers) {
        this.userService = userService;
        this.callbackDispatcher = callbackDispatcher;
        this.interceptors = interceptors;

        this.stateHandlers = stateHandlers.stream()
                .collect(Collectors.toMap(StateHandler::getSupportedState, Function.identity()));
    }

    public void handle(Update update) {
        if (update.hasCallbackQuery()) {
            callbackDispatcher.handleCallback(update.getCallbackQuery());
            return;
        }

        if (!update.hasMessage() || !update.getMessage().hasText()) return;

        long chatId = update.getMessage().getChatId();
        String text = update.getMessage().getText();
        UserEntity session = userService.findOrCreateById(chatId);

        boolean needsSave = false;

        if (session.getLanguageCode() == null) {
            String telegramLang = update.getMessage().getFrom().getLanguageCode();
            session.setLanguageCode(telegramLang);
            needsSave = true;
        }

        String currentName = update.getMessage().getFrom().getFirstName();
        if (!currentName.equals(session.getFirstName())) {
            session.setFirstName(currentName);
            needsSave = true;
        }

        if (needsSave) {
            userService.save(session);
        }

        for (TextInterceptor interceptor : interceptors) {
            if (interceptor.handle(session, text)) {
                return;
            }
        }

        StateHandler handler = stateHandlers.getOrDefault(session.getUserState(), stateHandlers.get(UserState.MAIN_MENU));
        handler.handle(session, text);
    }
}