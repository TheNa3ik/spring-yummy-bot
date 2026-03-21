package com.thena3ik.mealplanner.service;

import com.thena3ik.mealplanner.model.entity.RecipeEntity;
import com.thena3ik.mealplanner.model.entity.RecipeTranslationEntity;
import com.thena3ik.mealplanner.repository.RecipeTranslationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class TranslationService {

    private final GeminiService geminiService;
    private final RecipeTranslationRepository translationDao;

    public TranslationService(GeminiService geminiService, RecipeTranslationRepository translationDao) {
        this.geminiService = geminiService;
        this.translationDao = translationDao;
    }

    public RecipeTranslationEntity getTranslatedCard(RecipeEntity englishRecipe, String langCode) {
        if ("en".equalsIgnoreCase(langCode)) return null;

        Optional<RecipeTranslationEntity> optTrans = translationDao.findFirstByRecipeIdAndLanguageCode(englishRecipe.getId(), langCode);

        if (optTrans.isPresent()) return optTrans.get();

        log.info("No cached translation found. Translating CARD for Recipe [ID: {}] to language: '{}'", englishRecipe.getId(), langCode);

        String translatedTitle = geminiService.translateTextAsync(englishRecipe.getTitle(), langCode).join();

        RecipeTranslationEntity translation = new RecipeTranslationEntity();
        translation.setRecipe(englishRecipe);
        translation.setLanguageCode(langCode);
        translation.setTitle(translatedTitle);
        translation.setDetailsTranslated(false);

        return translationDao.save(translation);
    }

    public RecipeTranslationEntity getTranslationDetails(RecipeEntity englishRecipe, String langCode) {
        if ("en".equalsIgnoreCase(langCode)) return null;

        RecipeTranslationEntity translation = getTranslatedCard(englishRecipe, langCode);

        if (translation.isDetailsTranslated()) return translation;

        log.info("Translating DETAILS (Summary & Ingredients) for Recipe [ID: {}] to language: '{}'", englishRecipe.getId(), langCode);

        String rawIngredients = englishRecipe.getIngredientsList() != null ? englishRecipe.getIngredientsList() : "";

        CompletableFuture<String> summaryFuture = geminiService.translateTextAsync(englishRecipe.getSummary(), langCode);
        CompletableFuture<String> ingredientsFuture = geminiService.translateTextAsync(rawIngredients, langCode);

        try {
            CompletableFuture.allOf(summaryFuture, ingredientsFuture).join();

            translation.setSummary(summaryFuture.get());
            translation.setTranslatedIngredients(ingredientsFuture.get());

            translation.setDetailsTranslated(true);
            return translationDao.save(translation);

        } catch (Exception e) {
            log.error("Parallel translation failed for Recipe [ID: {}]. Falling back to English.", englishRecipe.getId(), e);
            return translation;
        }
    }
}
