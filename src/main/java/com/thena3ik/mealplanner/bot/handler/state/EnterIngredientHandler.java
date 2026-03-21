package com.thena3ik.mealplanner.bot.handler.state;

import com.thena3ik.mealplanner.model.entity.RecipeEntity;
import com.thena3ik.mealplanner.model.entity.SearchStateEntity;
import com.thena3ik.mealplanner.model.entity.UserEntity;
import com.thena3ik.mealplanner.model.enums.UserState;
import com.thena3ik.mealplanner.repository.RecipeRepository;
import com.thena3ik.mealplanner.service.*;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class EnterIngredientHandler implements StateHandler {

    private final TelegramService telegramService;
    private final LocaleService localeService;
    private final UserService userService;
    private final SpoonacularService spoonacularService;
    private final RecipeRepository recipeRepository;
    private final RecipeCardService recipeCardService;

    public EnterIngredientHandler (TelegramService telegramService,
                                   LocaleService localeService,
                                   UserService userService,
                                   SpoonacularService spoonacularService,
                                   RecipeRepository recipeRepository,
                                   RecipeCardService recipeCardService) {
        this.telegramService = telegramService;
        this.localeService = localeService;
        this.userService = userService;
        this.spoonacularService = spoonacularService;
        this.recipeRepository = recipeRepository;
        this.recipeCardService = recipeCardService;
    }

    @Override
    public UserState getSupportedState() {
        return UserState.ENTER_INGREDIENTS;
    }

    @Override
    public void handle(UserEntity session, String text) {
        text = text.trim();
        SearchStateEntity searchState = session.getSearchState();
        searchState.setIngredients(text);

        if (sendRecipe(session)) {
            session.getSearchState().setUserState(UserState.SHOW_RECIPES);
            userService.save(session);
        }
    }

    private boolean sendRecipe(UserEntity session) {
        String lang = session.getLanguageCode();
        SearchStateEntity searchState = session.getSearchState();

        Optional<RecipeEntity> apiRecipeOpt = spoonacularService.searchSingleRecipe(
                searchState.getIngredients(), session.getDiet(), searchState.getOffset());

        if (apiRecipeOpt.isPresent()) {
            RecipeEntity recipe = recipeRepository.save(apiRecipeOpt.get());

            String text = recipeCardService.processAndFormatCard(session, recipe);

            telegramService.sendRecipeMessage(session, text, recipe.getId());
            return true;
        } else {
            telegramService.sendMainMenu(session, localeService.getMessage("flow.search.error.no_results", lang));
            return false;
        }
    }
}
