package com.thena3ik.mealplanner.bot.handler.core;

import com.thena3ik.mealplanner.model.entity.RecipeEntity;
import com.thena3ik.mealplanner.model.entity.SearchStateEntity;
import com.thena3ik.mealplanner.model.entity.UserEntity;
import com.thena3ik.mealplanner.repository.RecipeRepository;
import com.thena3ik.mealplanner.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

import java.util.Optional;

@Slf4j
@Component
public class CallbackDispatcher {

    private final TelegramService telegramService;
    private final LocaleService localeService;
    private final UserService userService;
    private final SpoonacularService spoonacularService;
    private final RecipeRepository recipeRepository;
    private final RecipeCardService recipeCardService;

    public CallbackDispatcher(TelegramService telegramService,
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

    public void handleCallback(CallbackQuery callback) {
        String data = callback.getData();
        String callbackId = callback.getId();
        int messageId = callback.getMessage().getMessageId();

        UserEntity session = userService.findOrCreateById(callback.getMessage().getChatId());
        SearchStateEntity searchState = session.getSearchState();

        try {
            switch (data) {
                case "next" -> {
                    searchState.incrementOffset();
                    userService.save(session);
                    updateRecipeMessage(session, messageId, callbackId);
                }
                case "prev" -> {
                    searchState.decrementOffset();
                    userService.save(session);
                    updateRecipeMessage(session, messageId, callbackId);
                }
                default -> {
                    if (data.startsWith("details_")) {
                        int recipeId = Integer.parseInt(data.substring("details_".length()));
                        showRecipeDetails(session, recipeId, callbackId);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to process callback query '{}' for user [ID: {}]", data, session.getChatId(), e);
            telegramService.answerCallback(callback.getId(),
                    localeService.getMessage("general.error.generic", session.getLanguageCode()));
        }
    }

    private void updateRecipeMessage(UserEntity session, int messageId, String callbackId) {
        String lang = session.getLanguageCode();
        SearchStateEntity searchState = session.getSearchState();

        Optional<RecipeEntity> apiRecipeOpt = spoonacularService.searchSingleRecipe(
                searchState.getIngredients(), session.getDiet(), searchState.getOffset());

        if (apiRecipeOpt.isPresent()) {
            RecipeEntity apiRecipe = apiRecipeOpt.get();
            RecipeEntity recipe = recipeRepository.findById(apiRecipe.getId())
                    .orElseGet(() -> recipeRepository.save(apiRecipe));


            String text = recipeCardService.processAndFormatCard(session, recipe);

            telegramService.editMessage(session.getChatId(), messageId, text,
                    telegramService.recipeInlineButtons(session, recipe.getId()));

            telegramService.answerCallback(callbackId, "");
        } else {
            telegramService.editMessage(session.getChatId(), messageId,
                    localeService.getMessage("flow.search.error.end_of_list", lang), null);
        }
    }

    private void showRecipeDetails(UserEntity session, int recipeId, String callbackId) {
        String lang = session.getLanguageCode();

        Optional<String> formattedDetailsOpt = recipeCardService.processAndFormatDetails(session, recipeId);

        if (formattedDetailsOpt.isPresent()) {
            telegramService.sendMessage(session.getChatId(), formattedDetailsOpt.get());
        } else {
            telegramService.sendMessage(session.getChatId(),
                    localeService.getMessage("flow.recipe.error.missing_details", lang));
        }

        telegramService.answerCallback(callbackId, "");
    }
}
