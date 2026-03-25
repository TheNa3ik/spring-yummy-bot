package com.thena3ik.yummybot.bot.handler.interceptors;

import com.thena3ik.yummybot.model.entity.UserEntity;
import com.thena3ik.yummybot.model.enums.BotCommand;
import com.thena3ik.yummybot.bot.handler.command.BotCommandHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@Order(1)
public class CommandInterceptor implements TextInterceptor {

    private final Map<BotCommand, BotCommandHandler> commandHandlers;

    @Autowired
    public CommandInterceptor(List<BotCommandHandler> handlers) {
        this.commandHandlers = handlers.stream()
                .collect(Collectors.toMap(BotCommandHandler::getSupportedCommand, Function.identity()));
    }

    @Override
    public boolean handle(UserEntity session, String text) {
        if (!text.startsWith("/")) {
            return false;
        }

        BotCommand command = BotCommand.fromText(text);

        if (command == null) {
            return true;
        }

        BotCommandHandler handler = commandHandlers.get(command);
        if (handler != null) {
            handler.handle(session, text);
            return true;
        }

        return false;
    }
}
