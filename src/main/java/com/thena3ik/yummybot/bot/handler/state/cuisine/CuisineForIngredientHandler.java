package com.thena3ik.yummybot.bot.handler.state.cuisine;

import com.thena3ik.yummybot.model.enums.UserState;
import com.thena3ik.yummybot.service.NavigationService;
import org.springframework.stereotype.Component;

@Component
public class CuisineForIngredientHandler extends AbstractCuisineHandler {

    public CuisineForIngredientHandler(NavigationService navigationService) {
        super(navigationService);
    }

    @Override
    public UserState getSupportedState() {
        return UserState.CUISINE_FOR_INGREDIENT;
    }
}
