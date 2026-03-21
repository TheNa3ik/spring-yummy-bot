package com.thena3ik.mealplanner.service;

import com.thena3ik.mealplanner.model.dto.RecipeDetails;
import com.thena3ik.mealplanner.model.dto.RecipeView;
import com.thena3ik.mealplanner.model.entity.*;
import com.thena3ik.mealplanner.repository.RecipeRepository;
import com.thena3ik.mealplanner.ui.MessageFormatter;
import com.thena3ik.mealplanner.ui.RecipeViewMapper;
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
        SearchStateEntity searchState = session.getSearchState();

        searchState.setRecipeId(recipe.getId());
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

        if (entityOpt.isEmpty() || !entityOpt.get().isDetailsFetched()) {
            Optional<RecipeDetails> detailsOpt = spoonacularService.getRecipeDetails(recipeId);
            if (detailsOpt.isPresent()) {
                RecipeEntity recipe = entityOpt.orElseGet(RecipeEntity::new);
                recipe.setId(recipeId);
                recipe.updateWithDetails(detailsOpt.get());
                recipeRepository.save(recipe);
                entityOpt = Optional.of(recipe);
            } else {
                log.warn("Spoonacular API failed to return details for Recipe [ID: {}]", recipeId);
                return Optional.empty();
            }
        }

        RecipeEntity recipe = entityOpt.get();

        RecipeTranslationEntity translation = null;
        if (session.isAiTranslationEnabled()) {
            translation = translationService.getTranslationDetails(recipe, lang);
        }

        RecipeView recipeView = recipeViewMapper.map(recipe, translation, lang);
        return Optional.of(messageFormatter.formatRecipeDetails(recipeView, lang));
    }
}