package com.thena3ik.yummybot.bot.handler.callback;

import com.thena3ik.yummybot.model.entity.RecipeEntity;
import com.thena3ik.yummybot.model.entity.SearchStateEntity;
import com.thena3ik.yummybot.model.entity.UserEntity;
import com.thena3ik.yummybot.model.enums.Cuisine;
import com.thena3ik.yummybot.model.enums.UserState;
import com.thena3ik.yummybot.service.*;
import com.thena3ik.yummybot.ui.InlineKeyboardFactory;
import com.thena3ik.yummybot.ui.ReplyKeyboardFactory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;

import java.util.Optional;

@Component
public class CuisineCallbackHandler implements CallbackHandler {

    private final UserService userService;
    private final TelegramService telegramService;
    private final LocaleService localeService;
    private final SpoonacularService spoonacularService;
    private final RecipeDeliveryService recipeDeliveryService;
    private final InlineKeyboardFactory inlineFactory;
    private final ReplyKeyboardFactory replyFactory;

    public CuisineCallbackHandler(UserService userService,
                                  TelegramService telegramService,
                                  LocaleService localeService,
                                  SpoonacularService spoonacularService,
                                  RecipeDeliveryService recipeDeliveryService,
                                  InlineKeyboardFactory inlineFactory,
                                  ReplyKeyboardFactory replyFactory) {
        this.userService = userService;
        this.telegramService = telegramService;
        this.localeService = localeService;
        this.spoonacularService = spoonacularService;
        this.recipeDeliveryService = recipeDeliveryService;
        this.inlineFactory = inlineFactory;
        this.replyFactory = replyFactory;
    }

    @Override
    public String getSupportedPrefix() {
        return "cuisine_";
    }

    @Override
    public void handle(UserEntity session, CallbackQuery callback) {
        String data = callback.getData();
        String callbackId = callback.getId();
        long chatId = session.getChatId();
        int messageId = callback.getMessage().getMessageId();
        String lang = session.getLanguageCode();

        SearchStateEntity searchState = session.getSearchState();
        if (searchState == null) {
            searchState = new SearchStateEntity();
            session.setSearchState(searchState);
        }

        if (data.equals("cuisine_ignore")) {
            telegramService.answerCallback(callbackId, null);
            return;
        }

        if (data.startsWith("cuisine_page_")) {
            int page = Integer.parseInt(data.replace("cuisine_page_", ""));

            InlineKeyboardMarkup keyboard = inlineFactory.getCuisineKeyboard(session, page);
            telegramService.editReplyMarkup(chatId, messageId, keyboard);
            telegramService.answerCallback(callbackId, null);
            return;
        }

        String finalText;
        if (data.equals("cuisine_skip")) {
            searchState.setCuisine(null);
            finalText = localeService.getMessage("menu.cuisine.saved.none", lang);
        } else {
            String cuisineCode = data.replace("cuisine_set_", "");
            Cuisine selectedCuisine = Cuisine.valueOf(cuisineCode.toUpperCase());
            searchState.setCuisine(selectedCuisine.getCode());
            finalText = localeService.getMessage("menu.cuisine.saved.selected", lang,
                    localeService.getMessage(selectedCuisine.getLabelText(), lang));
        }

        telegramService.editMessage(chatId, messageId, finalText, null);
        telegramService.answerCallback(callbackId, null);

        UserState currentState = session.getUserState();

        if (currentState == UserState.CUISINE_FOR_INGREDIENT) {
            session.setUserState(UserState.ENTER_INGREDIENTS);
            userService.save(session);

            String promptText = localeService.getMessage("flow.search.prompt.ingredients", lang);

            InlineKeyboardMarkup keyboard = inlineFactory.getCancelKeyboard(session, "");
            telegramService.sendMessage(chatId, promptText, keyboard);

        } else if (currentState == UserState.CUISINE_FOR_NAME) {
            session.setUserState(UserState.ENTER_NAME);
            userService.save(session);

            String promptText = localeService.getMessage("flow.search.prompt.name", lang);

            InlineKeyboardMarkup keyboard = inlineFactory.getCancelKeyboard(session, "");
            telegramService.sendMessage(chatId, promptText, keyboard);

        } else if (currentState == UserState.CUISINE_FOR_RANDOM) {
            String promptText = localeService.getMessage("flow.search.prompt.random", lang);

            ReplyKeyboardMarkup keyboard = replyFactory.getRecipeMenuKeyboard(session);
            telegramService.sendMessage(chatId, promptText, keyboard);

            Optional<RecipeEntity> apiRecipeOpt = spoonacularService.getRandomRecipe(session);

            if (recipeDeliveryService.processAndDeliver(session, apiRecipeOpt)) {
                session.setUserState(UserState.SHOW_RECIPES);
                userService.save(session);
            }
        }
    }
}