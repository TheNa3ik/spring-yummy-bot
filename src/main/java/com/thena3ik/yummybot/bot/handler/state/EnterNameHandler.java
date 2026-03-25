package com.thena3ik.yummybot.bot.handler.state;

import com.thena3ik.yummybot.model.entity.RecipeEntity;
import com.thena3ik.yummybot.model.entity.SearchStateEntity;
import com.thena3ik.yummybot.model.entity.UserEntity;
import com.thena3ik.yummybot.model.enums.UserState;
import com.thena3ik.yummybot.service.NavigationService;
import com.thena3ik.yummybot.service.RecipeDeliveryService;
import com.thena3ik.yummybot.service.SpoonacularService;
import com.thena3ik.yummybot.service.UserService;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class EnterNameHandler implements StateHandler {

    private final UserService userService;
    private final SpoonacularService spoonacularService;
    private final RecipeDeliveryService recipeDeliveryService;
    private final NavigationService navigationService;

    public EnterNameHandler(UserService userService,
                            SpoonacularService spoonacularService,
                            RecipeDeliveryService recipeDeliveryService,
                            NavigationService navigationService) {
        this.userService = userService;
        this.spoonacularService = spoonacularService;
        this.recipeDeliveryService = recipeDeliveryService;
        this.navigationService = navigationService;
    }

    @Override
    public UserState getSupportedState() {
        return UserState.ENTER_NAME;
    }

    @Override
    public void handle(UserEntity session, String text) {
        if (navigationService.handleBackButtonToMain(session, text)) {
            return;
        }

        SearchStateEntity searchState = session.getSearchState();
        if (searchState == null) {
            searchState = new SearchStateEntity();
            session.setSearchState(searchState);
        }

        searchState.setSearchName(text.trim());
        userService.save(session);

        Optional<RecipeEntity> apiRecipeOpt = spoonacularService.searchByName(session);

        if (recipeDeliveryService.processAndDeliver(session, apiRecipeOpt)) {
            session.setUserState(UserState.SHOW_RECIPES);
            userService.save(session);
        }
    }
}