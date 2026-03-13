package com.thena3ik.mealplanner.bot.handlers;

import com.thena3ik.mealplanner.models.LastSearch;
import com.thena3ik.mealplanner.models.Recipe;
import com.thena3ik.mealplanner.models.RecipeDetails;
import com.thena3ik.mealplanner.models.user.UserSession;
import com.thena3ik.mealplanner.service.*;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

import java.util.Optional;

@Component
public class CallbackQueryHandler {

    private final TelegramService telegramService;
    private final LocaleService localeService;
    private final UserService userService;
    private final SpoonacularService spoonacularService;
    private final MessageFormatter messageFormatter;

    public CallbackQueryHandler (TelegramService telegramService,
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

    public void handleCallback(CallbackQuery callback) {
        String data = callback.getData();
        long chatId = callback.getMessage().getChatId();
        int messageId = callback.getMessage().getMessageId();

        UserSession session = userService.findOrCreateById(chatId);
        LastSearch lastSearch = session.getLastSearch();
        String lang = session.getLanguageCode();

        telegramService.answerCallback(callback.getId(), "");

        try {
            switch (data) {
                case "next" -> {
                    lastSearch.incrementOffset();
                    userService.save(session);
                    updateRecipeMessage(session, messageId);
                }
                case "prev" -> {
                    lastSearch.decrementOffset();
                    userService.save(session);
                    updateRecipeMessage(session, messageId);
                }
                default -> {
                    if (data.startsWith("details_")) {
                        int recipeId = Integer.parseInt(data.substring("details_".length()));
                        showRecipeDetails(chatId, recipeId, lang);
                    }
                }
            }
        } catch (Exception e) {
            telegramService.answerCallback(callback.getId(), localeService.getMessage("error.generic", lang));
        }
    }

    private void updateRecipeMessage(UserSession session, int messageId) {
        LastSearch search = session.getLastSearch();
        String lang = session.getLanguageCode();

        Optional<Recipe> recipeOpt = spoonacularService.searchSingleRecipe(
                search.getIngredients(),
                search.getDiet(),
                search.getOffset()
        );

        if (recipeOpt.isPresent()) {
            Recipe recipe = recipeOpt.get();
            String text = messageFormatter.formatRecipeCard(recipe);
            telegramService.editMessage(session.getChatId(), messageId, text,
                    telegramService.recipeInlineButtons(recipe.getId(), lang));
        } else {
            telegramService.editMessage(session.getChatId(), messageId,
                    localeService.getMessage("search.end", lang), null);
        }
    }

    private void showRecipeDetails(long chatId, int recipeId, String lang) {
        Optional<RecipeDetails> detailsOpt = spoonacularService.getRecipeDetails(recipeId);


        if (detailsOpt.isPresent()) {
            String text = messageFormatter.formatRecipeDetails(detailsOpt.get());
            telegramService.sendMessage(chatId, text);
        } else {
            telegramService.sendMessage(chatId, localeService.getMessage("recipe.details.missing", lang));
        }
    }
}
