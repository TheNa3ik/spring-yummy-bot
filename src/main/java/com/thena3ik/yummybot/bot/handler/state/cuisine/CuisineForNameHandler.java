package com.thena3ik.yummybot.bot.handler.state.cuisine;

import com.thena3ik.yummybot.model.enums.UserState;
import com.thena3ik.yummybot.service.NavigationService;
import org.springframework.stereotype.Component;

@Component
public class CuisineForNameHandler extends AbstractCuisineHandler{

    public CuisineForNameHandler(NavigationService navigationService) {
        super(navigationService);
    }

    @Override
    public UserState getSupportedState() {
        return UserState.CUISINE_FOR_NAME;
    }
}
