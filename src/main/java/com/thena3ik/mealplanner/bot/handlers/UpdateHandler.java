package com.thena3ik.mealplanner.bot.handlers;

import com.thena3ik.mealplanner.bot.commands.BotCommand;
import com.thena3ik.mealplanner.models.Diet;
import com.thena3ik.mealplanner.models.LastSearch;
import com.thena3ik.mealplanner.models.Recipe;
import com.thena3ik.mealplanner.models.RecipeDetails;
import com.thena3ik.mealplanner.models.user.UserSession;
import com.thena3ik.mealplanner.models.user.UserState;
import com.thena3ik.mealplanner.service.MessageFormatter;
import com.thena3ik.mealplanner.service.SpoonacularService;
import com.thena3ik.mealplanner.service.TelegramService;
import com.thena3ik.mealplanner.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Optional;

@Component
public class UpdateHandler {

    private final TelegramService telegramService;
    private final SpoonacularService spoonacularService;
    private final UserService userService;
    private final MessageFormatter messageFormatter;

    @Autowired
    public UpdateHandler(TelegramService telegramService, SpoonacularService spoonacularService,
                         UserService userService, MessageFormatter messageFormatter) {
        this.telegramService = telegramService;
        this.spoonacularService = spoonacularService;
        this.userService = userService;
        this.messageFormatter = messageFormatter;
    }

    public void handle(Update update) {
        if (update.hasCallbackQuery()) {
            handleCallback(update);
            return;
        }

        if (!update.hasMessage() || !update.getMessage().hasText()) return;

        long chatId = update.getMessage().getChatId();
        String text = update.getMessage().getText();
        UserSession session = userService.findOrCreateById(chatId);
        UserState state = session.getUserState();

        session.setFirstName(update.getMessage().getFrom().getFirstName());

        BotCommand command = BotCommand.fromText(text);
        if (command != null) {
            handleCommand(session, command);
            return;
        }

        switch (state) {
            case CHOOSE_DIET -> handleChooseDiet(session, text);
            case ENTER_INGREDIENTS -> handleEnterIngredients(session, text);
            default -> handleIdle(session);
        }
    }

    private void handleCommand(UserSession session, BotCommand command) {
        long chatId = session.getChatId();

        switch (command) {
            case START -> handleStart(session);

            case SEARCH -> handleSearch(session);

            case DIET_PREFERENCES -> {
                session.setUserState(UserState.CHOOSE_DIET);
                userService.save(session);
                telegramService.sendDietKeyboard(chatId,
                        "🥗 **Choose your diet**\n\n(This helps me find the perfect recipes for you!)");
            }

            default -> telegramService.sendMainMenu(chatId, "🤔 **Oops!** I don't recognize that command.");
        }
    }


    private void handleStart(UserSession session) {
        if (!session.hasDiet()) {
            session.setUserState(UserState.CHOOSE_DIET);
            String text = String.format("""
                👋 **Welcome, %s!**
                
                I'm your Meal Planner Bot 🤖
                Before we start, please choose your diet so I can find the best meals for you. 👇
                """,
                    session.getFirstName());
            telegramService.sendDietKeyboard(session.getChatId(), text);
        } else {
            String text = String.format("""
                            👋 **Welcome back, %s!**
                            
                            Let's find your next meal! 🍽️
                            """,
                    session.getFirstName());
            telegramService.sendMainMenu(session.getChatId(), text);
        }
    }

    private void handleSearch (UserSession session) {
        LastSearch search = session.getLastSearch();

        if (search == null) {
            search = new LastSearch("none", "", 0);
            session.setLastSearch(search);
        }

        search.resetOffset();
        search.setIngredients("");

        session.setUserState(UserState.ENTER_INGREDIENTS);
        userService.save(session);
        telegramService.sendMessage(session.getChatId(),
                "🛒 **What ingredients do you have?**\n\n(e.g., *chicken, broccoli, rice*)");
    }

    private void handleChooseDiet(UserSession session, String dietTextFromButton) {
        if (!telegramService.isValidDietOption(dietTextFromButton)) {
            telegramService.sendMessage(session.getChatId(), "⚠️ **Invalid option!**\n\nPlease tap one of the buttons below. 👇");
            return;
        }

        Diet selectedDiet = Diet.fromDisplayText(dietTextFromButton).get();

        String dietApiValue = selectedDiet.getApiValue();

        session.setLastSearch(new LastSearch(dietApiValue, "", 0));
        session.setUserState(UserState.IDLE);
        userService.save(session);

        telegramService.sendMainMenu(session.getChatId(),
                String.format("✅ **Got it!**\n\nYour diet is set to: **%s**", selectedDiet.getDisplayText()));
    }

    private void handleEnterIngredients(UserSession session, String ingredients) {
        ingredients = ingredients.trim();
        LastSearch lastSearch = session.getLastSearch();
        lastSearch.setIngredients(ingredients);

        if (sendRecipe(session)) {
            session.setUserState(UserState.SHOW_RECIPES);
            userService.save(session);
        }
    }

    private void handleCallback(Update update) {
        var callback = update.getCallbackQuery();
        String data = callback.getData();
        long chatId = callback.getMessage().getChatId();
        int messageId = callback.getMessage().getMessageId();

        UserSession session = userService.findOrCreateById(chatId);
        LastSearch lastSearch = session.getLastSearch();

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
                        showRecipeDetails(chatId, recipeId);
                    }
                }
            }
        } catch (Exception e) {
            telegramService.answerCallback(callback.getId(), "⚠️ Whoops! Something went wrong.");
        }
    }

    private void updateRecipeMessage(UserSession session, int messageId) {
        LastSearch search = session.getLastSearch();

        Optional<Recipe> recipeOpt = spoonacularService.searchSingleRecipe(
                search.getIngredients(),
                search.getDiet(),
                search.getOffset()
        );

        if (recipeOpt.isPresent()) {
            Recipe recipe = recipeOpt.get();
            String text = messageFormatter.formatRecipeCard(recipe);
            telegramService.editMessage(session.getChatId(), messageId, text,
                    telegramService.recipeInlineButtons(recipe.getId()));
        } else {
            telegramService.editMessage(session.getChatId(), messageId,
                    "😕 **That's all for now!**\n\nNo more recipes found for this search.", null);
        }
    }

    private void showRecipeDetails(long chatId, int recipeId) {
        Optional<RecipeDetails> detailsOpt = spoonacularService.getRecipeDetails(recipeId);

        if (detailsOpt.isPresent()) {
            String text = messageFormatter.formatRecipeDetails(detailsOpt.get());
            telegramService.sendMessage(chatId, text);
        } else {
            telegramService.sendMessage(chatId, "❌ **Sorry!**\n\nI couldn't find the details for that recipe.");
        }
    }

    private void handleIdle(UserSession session) {
        telegramService.sendMainMenu(session.getChatId(),
                "Ready when you are! 🤖\n\nUse the **Search** button to find a new meal.");
    }

    private boolean sendRecipe(UserSession session) {
        LastSearch search = session.getLastSearch();

        Optional<Recipe> recipeOpt = spoonacularService.searchSingleRecipe(
                search.getIngredients(),
                search.getDiet(),
                search.getOffset()
        );

        if (recipeOpt.isPresent()) {
            Recipe recipe = recipeOpt.get();
            search.setRecipeId(recipe.getId());
            userService.save(session);

            String text = messageFormatter.formatRecipeCard(recipe);
            telegramService.sendRecipeMessage(session.getChatId(), text, recipe.getId());
            return true;
        } else {
            telegramService.sendMainMenu(session.getChatId(), "😕 **No recipes found!**\n\nTry searching with different ingredients.");
            return false;
        }
    }
}
