package com.thena3ik.yummybot.service;

import com.thena3ik.yummybot.model.dto.Recipe;
import com.thena3ik.yummybot.model.dto.RecipeView;
import com.thena3ik.yummybot.model.entity.*;
import com.thena3ik.yummybot.repository.RecipeRepository;
import com.thena3ik.yummybot.ui.MessageFormatter;
import com.thena3ik.yummybot.ui.RecipeViewMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
public class RecipeCardService {

    private final UserService userService;
    private final TranslationService translationService;
    private final RecipeViewMapper recipeViewMapper;
    private final MessageFormatter messageFormatter;
    private final RecipeRepository recipeRepository;
    private final SpoonacularService spoonacularService;

    public RecipeCardService(UserService userService,
                             TranslationService translationService,
                             RecipeViewMapper recipeViewMapper,
                             MessageFormatter messageFormatter,
                             RecipeRepository recipeRepository,
                             SpoonacularService spoonacularService) {
        this.userService = userService;
        this.translationService = translationService;
        this.recipeViewMapper = recipeViewMapper;
        this.messageFormatter = messageFormatter;
        this.recipeRepository = recipeRepository;
        this.spoonacularService = spoonacularService;
    }

    public String processAndFormatCard(UserEntity session, RecipeEntity recipe) {
        String lang = session.getLanguageCode();

        session.getSearchState().setRecipeId(recipe.getId());
        userService.save(session);

        RecipeTranslationEntity translation = null;
        if (session.isAiTranslationEnabled()) {
            translation = translationService.getTranslatedCard(recipe, lang);
        }

        RecipeView recipeView = recipeViewMapper.map(recipe, translation, lang);
        return messageFormatter.formatRecipeCard(recipeView, lang);
    }

    public Optional<String> processAndFormatDetails(UserEntity session, int recipeId) {
        String lang = session.getLanguageCode();

        Optional<RecipeEntity> entityOpt = recipeRepository.findById(recipeId);

        if (entityOpt.isEmpty()) {
            log.error("Recipe [ID: {}] not found in database.", recipeId);
            return Optional.empty();
        }

        RecipeEntity recipe = entityOpt.get();

        if (recipe.getIngredientsList() == null || recipe.getIngredientsList().isBlank()) {
            log.info("Ingredients missing for Recipe [ID: {}]. Fetching full details from Spoonacular...", recipeId);

            Optional<Recipe> detailsOpt = spoonacularService.getRecipeDetails(recipeId);

            if (detailsOpt.isPresent()) {
                recipe.updateWithDetails(detailsOpt.get());
                recipeRepository.save(recipe);
            } else {
                log.warn("Spoonacular API failed to return full details for Recipe [ID: {}]", recipeId);
                return Optional.empty();
            }
        } else {
            log.debug("Cache hit! Ingredients already exist for Recipe [ID: {}] (Likely a Random Search).", recipeId);
        }

        RecipeTranslationEntity translation = null;
        if (session.isAiTranslationEnabled()) {
            translation = translationService.getTranslationDetails(recipe, lang);
        }

        RecipeView recipeView = recipeViewMapper.map(recipe, translation, lang);
        return Optional.of(messageFormatter.formatRecipeDetails(recipeView, lang));
    }
}