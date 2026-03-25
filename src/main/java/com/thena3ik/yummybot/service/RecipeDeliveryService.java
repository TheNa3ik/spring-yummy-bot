package com.thena3ik.yummybot.service;

import com.thena3ik.yummybot.model.entity.RecipeEntity;
import com.thena3ik.yummybot.model.entity.UserEntity;
import com.thena3ik.yummybot.model.enums.UserState;
import com.thena3ik.yummybot.repository.RecipeRepository;
import com.thena3ik.yummybot.ui.InlineKeyboardFactory;
import com.thena3ik.yummybot.ui.ReplyKeyboardFactory;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;

import java.util.Optional;

@Service
public class RecipeDeliveryService {

    private final RecipeRepository recipeRepository;
    private final RecipeCardService recipeCardService;
    private final TelegramService telegramService;
    private final LocaleService localeService;
    private final NavigationService navigationService;
    private final ReplyKeyboardFactory replyFactory;
    private final InlineKeyboardFactory inlineFactory;


    public RecipeDeliveryService(RecipeRepository recipeRepository,
                                 RecipeCardService recipeCardService,
                                 TelegramService telegramService,
                                 LocaleService localeService,
                                 NavigationService navigationService,
                                 ReplyKeyboardFactory replyFactory,
                                 InlineKeyboardFactory inlineFactory) {
        this.recipeRepository = recipeRepository;
        this.recipeCardService = recipeCardService;
        this.telegramService = telegramService;
        this.localeService = localeService;
        this.navigationService = navigationService;
        this.replyFactory = replyFactory;
        this.inlineFactory = inlineFactory;
    }

    public boolean processAndDeliver(UserEntity session, Optional<RecipeEntity> apiRecipeOpt) {
        String lang = session.getLanguageCode();
        long chatId = session.getChatId();

        if (apiRecipeOpt.isPresent()) {
            RecipeEntity recipe = recipeRepository.save(apiRecipeOpt.get());
            String cardText = recipeCardService.processAndFormatCard(session, recipe);

            if (session.getUserState() != UserState.CUISINE_FOR_RANDOM) {
                String successText = localeService.getMessage("flow.search.success", lang);

                ReplyKeyboardMarkup keyboard = replyFactory.getRecipeMenuKeyboard(session);
                telegramService.sendMessage(chatId, successText, keyboard);
            }

            InlineKeyboardMarkup recipeKeyboard = inlineFactory.getRecipeKeyboard(session, recipe.getId());
            telegramService.sendMessage(chatId, cardText, recipeKeyboard);

            return true;

        } else {
            String errorText = localeService.getMessage("flow.search.error.no_results", lang);

            session.setUserState(UserState.MAIN_MENU);

            ReplyKeyboardMarkup keyboard = replyFactory.getMainMenuKeyboard(session);
            telegramService.sendMessage(chatId, errorText, keyboard);
            navigationService.navigateToMainMenu(session);

            return false;
        }
    }
}
