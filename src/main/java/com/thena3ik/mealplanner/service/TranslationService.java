package com.thena3ik.mealplanner.service;

import com.thena3ik.mealplanner.models.entity.RecipeEntity;
import com.thena3ik.mealplanner.models.entity.RecipeTranslationEntity;
import com.thena3ik.mealplanner.repository.RecipeTranslationDao;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Service
public class TranslationService {

    private final GeminiService geminiService;
    private final RecipeTranslationDao translationDao;

    public TranslationService(GeminiService geminiService, RecipeTranslationDao translationDao) {
        this.geminiService = geminiService;
        this.translationDao = translationDao;
    }

    public RecipeTranslationEntity getTranslatedCard(RecipeEntity englishRecipe, String langCode) {
        if ("en".equalsIgnoreCase(langCode)) return null;

        Optional<RecipeTranslationEntity> optTrans = translationDao.findFirstByRecipeIdAndLanguageCode(englishRecipe.getId(), langCode);

        if (optTrans.isPresent()) return optTrans.get();

        String translatedTitle = geminiService.translateTextAsync(englishRecipe.getTitle(), langCode).join();

        RecipeTranslationEntity translation = new RecipeTranslationEntity();
        translation.setRecipeId(englishRecipe.getId());
        translation.setLanguageCode(langCode);
        translation.setTitle(translatedTitle);
        translation.setDetailsTranslated(false);

        return translationDao.save(translation);
    }

    public RecipeTranslationEntity getTranslationDetails(RecipeEntity englishRecipe, String langCode) {
        if ("en".equalsIgnoreCase(langCode)) return null;

        RecipeTranslationEntity translation = getTranslatedCard(englishRecipe, langCode);

        if (translation.isDetailsTranslated()) return translation;

        String ingredientsBlock = englishRecipe.getIngredients() != null ? String.join("\n🔹 ", englishRecipe.getIngredients()) : "";
        if (!ingredientsBlock.isEmpty()) ingredientsBlock = "🔹 " + ingredientsBlock;

        CompletableFuture<String> summaryFuture = geminiService.translateTextAsync(englishRecipe.getSummary(), langCode);
        CompletableFuture<String> ingredientsFuture = geminiService.translateTextAsync(ingredientsBlock, langCode);

        CompletableFuture.allOf(summaryFuture, ingredientsFuture).join();

        try {
            translation.setSummary(summaryFuture.get());
            translation.setTranslatedIngredients(ingredientsFuture.get());
        } catch (Exception e) {
            translation.setSummary("Translation error.");
            translation.setTranslatedIngredients("Translation error.");
        }

        translation.setDetailsTranslated(true);
        return translationDao.save(translation);
    }
}
