package com.thena3ik.yummybot.bot.handler.callback;

import com.thena3ik.yummybot.model.entity.RecipeEntity;
import com.thena3ik.yummybot.model.entity.SearchStateEntity;
import com.thena3ik.yummybot.model.entity.UserEntity;
import com.thena3ik.yummybot.repository.RecipeRepository;
import com.thena3ik.yummybot.service.*;
import com.thena3ik.yummybot.ui.InlineKeyboardFactory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.util.Optional;

@Component
public class RecipeCallbackHandler implements CallbackHandler {

    private final UserService userService;
    private final TelegramService telegramService;
    private final LocaleService localeService;
    private final SpoonacularService spoonacularService;
    private final RecipeRepository recipeRepository;
    private final RecipeCardService recipeCardService;
    private final InlineKeyboardFactory inlineFactory;

    public RecipeCallbackHandler(UserService userService,
                                 TelegramService telegramService,
                                 LocaleService localeService,
                                 SpoonacularService spoonacularService,
                                 RecipeRepository recipeRepository,
                                 RecipeCardService recipeCardService,
                                 InlineKeyboardFactory inlineFactory) {
        this.userService = userService;
        this.telegramService = telegramService;
        this.localeService = localeService;
        this.spoonacularService = spoonacularService;
        this.recipeRepository = recipeRepository;
        this.recipeCardService = recipeCardService;
        this.inlineFactory = inlineFactory;
    }

    @Override
    public String getSupportedPrefix() {
        return "recipe_";
    }

    @Override
    public void handle(UserEntity session, CallbackQuery callback) {
        String data = callback.getData();
        String callbackId = callback.getId();
        int messageId = callback.getMessage().getMessageId();
        SearchStateEntity searchState = session.getSearchState();

        if (data.equals("recipe_next")) {
            searchState.incrementOffset();
            userService.save(session);
            updateRecipeMessage(session, messageId, callbackId);
        } else if (data.equals("recipe_prev")) {
            if (searchState.getOffset() > 0) {
                searchState.decrementOffset();
                userService.save(session);
                updateRecipeMessage(session, messageId, callbackId);
            } else {
                String lang = session.getLanguageCode();
                String text = localeService.getMessage("flow.search.error.first_page", lang);

                telegramService.answerCallback(callbackId, text);
            }
        } else if (data.startsWith("recipe_details_")) {
            int recipeId = Integer.parseInt(data.substring("recipe_details_".length()));
            showRecipeDetails(session, recipeId, callbackId);
        }
    }

    private void updateRecipeMessage(UserEntity session, int messageId, String callbackId) {
        String lang = session.getLanguageCode();
        SearchStateEntity searchState = session.getSearchState();
        Optional<RecipeEntity> apiRecipeOpt;

        String nameQuery = searchState.getSearchName();
        String ingredientQuery = searchState.getIngredients();

        if (nameQuery != null && !nameQuery.isBlank()) {
            apiRecipeOpt = spoonacularService.searchByName(session);
        } else if (ingredientQuery != null && !ingredientQuery.isBlank()) {
            apiRecipeOpt = spoonacularService.searchByIngredients(session);
        } else {
            apiRecipeOpt = spoonacularService.getRandomRecipe(session);
        }

        if (apiRecipeOpt.isPresent()) {
            RecipeEntity apiRecipe = apiRecipeOpt.get();

            RecipeEntity recipe = recipeRepository.findById(apiRecipe.getId())
                    .orElseGet(() -> recipeRepository.save(apiRecipe));

            String text = recipeCardService.processAndFormatCard(session, recipe);

            InlineKeyboardMarkup keyboard = inlineFactory.getRecipeKeyboard(session, recipe.getId());

            telegramService.editMessage(session.getChatId(), messageId, text, keyboard);
            telegramService.answerCallback(callbackId, null);
        } else {
            String text = localeService.getMessage("flow.search.error.end_of_list", lang);
            telegramService.answerCallback(callbackId, text);
        }
    }

    private void showRecipeDetails(UserEntity session, int recipeId, String callbackId) {
        String lang = session.getLanguageCode();
        long chatId = session.getChatId();

        Optional<String> formattedDetailsOpt = recipeCardService.processAndFormatDetails(session, recipeId);

        if (formattedDetailsOpt.isPresent()) {
            InlineKeyboardMarkup keyboard = inlineFactory.getCancelKeyboard(session, "recipe");
            telegramService.sendMessage(chatId, formattedDetailsOpt.get(), keyboard);
        } else {
            String text = localeService.getMessage("flow.recipe.error.missing_details", lang);
            telegramService.sendMessage(chatId, text);
        }

        telegramService.answerCallback(callbackId, null);
    }
}
