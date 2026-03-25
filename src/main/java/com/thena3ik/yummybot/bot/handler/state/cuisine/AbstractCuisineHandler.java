package com.thena3ik.yummybot.bot.handler.state.cuisine;

import com.thena3ik.yummybot.bot.handler.state.StateHandler;
import com.thena3ik.yummybot.model.entity.UserEntity;
import com.thena3ik.yummybot.service.NavigationService;

public abstract class AbstractCuisineHandler implements StateHandler {

    private final NavigationService navigationService;

    public AbstractCuisineHandler(NavigationService navigationService) {
        this.navigationService = navigationService;
    }

    @Override
    public void handle(UserEntity session, String text) {
        navigationService.sendUnknownCommandError(session, null);
    }
}