package com.thena3ik.mealplanner.bot.handlers;

import com.thena3ik.mealplanner.components.MessageFormatter;
import com.thena3ik.mealplanner.models.user.LastSearch;
import com.thena3ik.mealplanner.models.dto.RecipeDetails;
import com.thena3ik.mealplanner.models.entity.RecipeEntity;
import com.thena3ik.mealplanner.models.entity.RecipeTranslationEntity;
import com.thena3ik.mealplanner.models.user.UserSession;
import com.thena3ik.mealplanner.repository.RecipeDao;
import com.thena3ik.mealplanner.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

import java.util.Optional;

@Slf4j
@Component
public class CallbackQueryHandler {

    private final TelegramService telegramService;
    private final LocaleService localeService;
    private final UserService userService;
    private final SpoonacularService spoonacularService;
    private final MessageFormatter messageFormatter;
    private final RecipeDao recipeDao;
    private final TranslationService translationService;

    public CallbackQueryHandler (TelegramService telegramService,
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

    public void handleCallback(CallbackQuery callback) {
        String data = callback.getData();
        long chatId = callback.getMessage().getChatId();
        int messageId = callback.getMessage().getMessageId();

        UserSession session = userService.findOrCreateById(chatId);
        LastSearch lastSearch = session.getLastSearch();
        String lang = session.getLanguageCode();

        String callbackId = callback.getId();

        try {
            switch (data) {
                case "next" -> {
                    lastSearch.incrementOffset();
                    userService.save(session);
                    updateRecipeMessage(session, messageId, callbackId);
                }
                case "prev" -> {
                    lastSearch.decrementOffset();
                    userService.save(session);
                    updateRecipeMessage(session, messageId, callbackId);
                }
                default -> {
                    if (data.startsWith("details_")) {
                        int recipeId = Integer.parseInt(data.substring("details_".length()));
                        showRecipeDetails(chatId, recipeId, lang, callbackId);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to process callback query '{}' for user [ID: {}]", data, chatId, e);
            telegramService.answerCallback(callback.getId(), localeService.getMessage("error.generic", lang));
        }
    }

    private void updateRecipeMessage(UserSession session, int messageId, String callbackId) {
        String lang = session.getLanguageCode();
        LastSearch search = session.getLastSearch();

        Optional<RecipeEntity> apiRecipeOpt = spoonacularService.searchSingleRecipe(
                search.getIngredients(), search.getDiet(), search.getOffset());

        if (apiRecipeOpt.isPresent()) {
            RecipeEntity apiRecipe = apiRecipeOpt.get();

            RecipeEntity recipe = recipeDao.findById(apiRecipe.getId())
                    .orElseGet(() -> recipeDao.save(apiRecipe));

            search.setRecipeId(recipe.getId());
            userService.save(session);

            RecipeTranslationEntity translation = translationService.getTranslatedCard(recipe, lang);

            String text = messageFormatter.formatRecipeCard(recipe, translation, lang);
            telegramService.editMessage(session.getChatId(), messageId, text,
                    telegramService.recipeInlineButtons(recipe.getId(), lang));

            telegramService.answerCallback(callbackId, "");
        } else {
            telegramService.editMessage(session.getChatId(), messageId,
                    localeService.getMessage("search.end", lang), null);
        }
    }

    private void showRecipeDetails(long chatId, int recipeId, String lang, String callbackId) {
        Optional<RecipeEntity> entityOpt = recipeDao.findById(recipeId);

        if (entityOpt.isEmpty() || !entityOpt.get().isDetailsFetched()) {
            Optional<RecipeDetails> detailsOpt = spoonacularService.getRecipeDetails(recipeId);
            if (detailsOpt.isPresent()) {
                RecipeEntity recipe = entityOpt.orElseGet(RecipeEntity::new);
                recipe.setId(recipeId);
                recipe.updateWithDetails(detailsOpt.get());
                recipeDao.save(recipe);
                entityOpt = Optional.of(recipe);
            } else {
                log.warn("Spoonacular API failed to return details for Recipe [ID: {}]", recipeId);
                telegramService.sendMessage(chatId, localeService.getMessage("recipe.details.missing", lang));
                return;
            }
        }

        RecipeEntity recipe = entityOpt.get();

        RecipeTranslationEntity translation = translationService.getTranslationDetails(recipe, lang);

        String text = messageFormatter.formatRecipeDetails(recipe, translation, lang);
        telegramService.sendMessage(chatId, text);

        telegramService.answerCallback(callbackId, "");
    }
}
