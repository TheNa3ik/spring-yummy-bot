package com.thena3ik.yummybot.bot.handler.state;

import com.thena3ik.yummybot.model.entity.RecipeEntity;
import com.thena3ik.yummybot.model.entity.SearchStateEntity;
import com.thena3ik.yummybot.model.entity.UserEntity;
import com.thena3ik.yummybot.model.enums.UserState;
import com.thena3ik.yummybot.service.*;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class EnterIngredientHandler implements StateHandler {

    private final UserService userService;
    private final SpoonacularService spoonacularService;
    private final RecipeDeliveryService recipeDeliveryService;
    private final NavigationService navigationService;

    public EnterIngredientHandler (UserService userService,
                                   SpoonacularService spoonacularService,
                                   RecipeDeliveryService recipeDeliveryService,
                                   NavigationService navigationService) {
        this.recipeDeliveryService = recipeDeliveryService;
        this.userService = userService;
        this.spoonacularService = spoonacularService;
        this.navigationService = navigationService;
    }

    @Override
    public UserState getSupportedState() {
        return UserState.ENTER_INGREDIENTS;
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

        searchState.setIngredients(text.trim());

        userService.save(session);

        Optional<RecipeEntity> apiRecipeOpt = spoonacularService.searchByIngredients(session);

        if (recipeDeliveryService.processAndDeliver(session, apiRecipeOpt)) {
            session.setUserState(UserState.SHOW_RECIPES);
            userService.save(session);
        }
    }
}