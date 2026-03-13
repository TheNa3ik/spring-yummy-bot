package com.thena3ik.mealplanner.bot.handlers.state;

import com.thena3ik.mealplanner.models.LastSearch;
import com.thena3ik.mealplanner.models.Recipe;
import com.thena3ik.mealplanner.models.user.UserSession;
import com.thena3ik.mealplanner.models.user.UserState;
import com.thena3ik.mealplanner.service.*;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class EnterIngredientHandler implements StateHandler {

    private final TelegramService telegramService;
    private final LocaleService localeService;
    private final UserService userService;
    private final SpoonacularService spoonacularService;
    private final MessageFormatter messageFormatter;

    public EnterIngredientHandler (TelegramService telegramService,
                                   LocaleService localeService,
                                   UserService userService,
                                   SpoonacularService spoonacularService,
                                   MessageFormatter messageFormatter) {
        this.telegramService = telegramService;
        this.localeService = localeService;
        this.userService = userService;
        this.spoonacularService = spoonacularService;
        this.messageFormatter = messageFormatter;
    }

    @Override
    public UserState getSupportedState() {
        return UserState.ENTER_INGREDIENTS;
    }

    @Override
    public void handle(UserSession session, String text) {
        text = text.trim();
        LastSearch lastSearch = session.getLastSearch();
        lastSearch.setIngredients(text);

        if (sendRecipe(session)) {
            session.setUserState(UserState.SHOW_RECIPES);
            userService.save(session);
        }
    }

    private boolean sendRecipe(UserSession session) {
        String lang = session.getLanguageCode();
        LastSearch search = session.getLastSearch();

        Optional<Recipe> recipeOpt = spoonacularService.searchSingleRecipe(
                search.getIngredients(),
                search.getDiet(),
                search.getOffset()
        );

        if (recipeOpt.isPresent()) {
            Recipe recipe = recipeOpt.get();
            search.setRecipeId(recipe.getId());
            userService.save(session);

            String text = messageFormatter.formatRecipeCard(recipe);
            telegramService.sendRecipeMessage(session.getChatId(), text, recipe.getId(), lang);
            return true;
        } else {
            telegramService.sendMainMenu(session.getChatId(), localeService.getMessage("search.no_results", lang), lang);
            return false;
        }
    }
}
