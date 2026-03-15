package com.thena3ik.mealplanner.bot.handlers.state;

import com.thena3ik.mealplanner.models.LastSearch;
import com.thena3ik.mealplanner.models.entity.RecipeEntity;
import com.thena3ik.mealplanner.models.entity.RecipeTranslationEntity;
import com.thena3ik.mealplanner.models.user.UserSession;
import com.thena3ik.mealplanner.models.user.UserState;
import com.thena3ik.mealplanner.repository.RecipeDao;
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
    private final RecipeDao recipeDao;
    private final TranslationService translationService;

    public EnterIngredientHandler (TelegramService telegramService,
                                   LocaleService localeService,
                                   UserService userService,
                                   SpoonacularService spoonacularService,
                                   MessageFormatter messageFormatter,
                                   RecipeDao recipeDao,
                                   TranslationService translationService) {
        this.telegramService = telegramService;
        this.localeService = localeService;
        this.userService = userService;
        this.spoonacularService = spoonacularService;
        this.messageFormatter = messageFormatter;
        this.recipeDao = recipeDao;
        this.translationService = translationService;
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

        Optional<RecipeEntity> apiRecipeOpt = spoonacularService.searchSingleRecipe(
                search.getIngredients(), search.getDiet(), search.getOffset());

        if (apiRecipeOpt.isPresent()) {
            RecipeEntity recipe = recipeDao.save(apiRecipeOpt.get());
            search.setRecipeId(recipe.getId());
            userService.save(session);

            RecipeTranslationEntity translation = translationService.getTranslatedCard(recipe, lang);

            String text = messageFormatter.formatRecipeCard(recipe, translation, lang);
            telegramService.sendRecipeMessage(session.getChatId(), text, recipe.getId(), lang);
            return true;
        } else {
            telegramService.sendMainMenu(session.getChatId(), localeService.getMessage("search.no_results", lang), lang);
            return false;
        }
    }
}
