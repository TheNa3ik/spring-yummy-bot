package com.thena3ik.mealplanner.bot.handlers;

import com.thena3ik.mealplanner.bot.commands.BotCommand;
import com.thena3ik.mealplanner.models.*;
import com.thena3ik.mealplanner.models.user.UserSession;
import com.thena3ik.mealplanner.models.user.UserState;
import com.thena3ik.mealplanner.service.*;
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
    private final LocaleService localeService;

    @Autowired
    public UpdateHandler(TelegramService telegramService, SpoonacularService spoonacularService,
                         UserService userService, MessageFormatter messageFormatter, LocaleService localeService) {
        this.telegramService = telegramService;
        this.spoonacularService = spoonacularService;
        this.userService = userService;
        this.messageFormatter = messageFormatter;
        this.localeService = localeService;
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

        if (session.getLanguageCode() == null) {
            String telegramLang = update.getMessage().getFrom().getLanguageCode();
            session.setLanguageCode(telegramLang);
            userService.save(session);
        }

        session.setFirstName(update.getMessage().getFrom().getFirstName());
        UserState state = session.getUserState();

        BotCommand command = resolveCommand(text, session.getLanguageCode());

        if (command != null) {
            handleCommand(session, command);
            return;
        }

        switch (state) {
            case CHOOSE_DIET -> handleChooseDiet(session, text);
            case CHOOSE_LANGUAGE -> handleChooseLanguage(session, text);
            case ENTER_INGREDIENTS -> handleEnterIngredients(session, text);
            default -> handleIdle(session);
        }
    }

    private BotCommand resolveCommand(String text, String langCode) {
        BotCommand command = BotCommand.fromText(text);
        if (command != null) return command;

        if (text.equals(localeService.getMessage("menu.button.search", langCode))) {
            return BotCommand.SEARCH;
        }
        if (text.equals(localeService.getMessage("menu.button.diet", langCode))) {
            return BotCommand.DIET_PREFERENCES;
        }
        if (text.equals(localeService.getMessage("menu.button.language", langCode))) {
            return BotCommand.LANGUAGE;
        }

        return null;
    }

    private void handleCommand(UserSession session, BotCommand command) {
        long chatId = session.getChatId();
        String lang = session.getLanguageCode();

        switch (command) {
            case START -> handleStart(session);
            case SEARCH -> handleSearch(session);
            case DIET_PREFERENCES -> {
                session.setUserState(UserState.CHOOSE_DIET);
                userService.save(session);
                telegramService.sendDietKeyboard(chatId, localeService.getMessage("diet.choose", lang), lang);
            }
            case LANGUAGE -> {
                session.setUserState(UserState.CHOOSE_LANGUAGE);
                userService.save(session);
                telegramService.sendLanguageMenu(chatId, localeService.getMessage("language.choose", lang));
            }
            default -> telegramService.sendMainMenu(chatId, localeService.getMessage("error.command.unknown", lang), lang);
        }
    }


    private void handleStart(UserSession session) {
        String lang = session.getLanguageCode();
        if (!session.hasDiet()) {
            session.setUserState(UserState.CHOOSE_DIET);
            telegramService.sendDietKeyboard(session.getChatId(),
                    localeService.getMessage("welcome.new", lang, session.getFirstName()), lang);
        } else {
            telegramService.sendMainMenu(session.getChatId(),
                    localeService.getMessage("welcome.back", lang, session.getFirstName()), lang);
        }
    }

    private void handleSearch (UserSession session) {
        String lang = session.getLanguageCode();
        LastSearch search = session.getLastSearch();

        if (search == null) {
            search = new LastSearch("none", "", 0);
            session.setLastSearch(search);
        }

        search.resetOffset();
        search.setIngredients("");

        session.setUserState(UserState.ENTER_INGREDIENTS);
        userService.save(session);
        telegramService.sendMessage(session.getChatId(), localeService.getMessage("search.start", lang));
    }

    private void handleChooseDiet(UserSession session, String dietTextFromButton) {
        String lang = session.getLanguageCode();

        Diet selectedDiet = resolveDiet(dietTextFromButton, lang);

        if (selectedDiet == null) {
            telegramService.sendMessage(session.getChatId(), localeService.getMessage("error.invalid", lang));
            return;
        }


        String dietApiValue = selectedDiet.getApiValue();

        session.setLastSearch(new LastSearch(dietApiValue, "", 0));
        session.setUserState(UserState.IDLE);
        userService.save(session);

        String displayDietName = localeService.getMessage(selectedDiet.getLabelKey(), lang);
        telegramService.sendMainMenu(session.getChatId(),
                localeService.getMessage("diet.confirm", lang, displayDietName), lang);
    }

    private Diet resolveDiet(String text, String lang) {
        for (Diet diet : Diet.values()) {
            String translatedText = localeService.getMessage(diet.getLabelKey(), lang);
            if (translatedText.equals(text)) {
                return diet;
            }
        }
        return null;
    }

    private void handleChooseLanguage(UserSession session, String text) {
        Optional<Language> selectedLangOpt = Language.fromDisplayText(text);

        String currentLang = session.getLanguageCode();

        if (selectedLangOpt.isEmpty()) {
            telegramService.sendMessage(session.getChatId(), localeService.getMessage("error.invalid", currentLang));
            return;
        }

        Language selectedLang = selectedLangOpt.get();
        session.setLanguageCode(selectedLang.getCode());
        session.setUserState(UserState.IDLE);
        userService.save(session);

        String newLangCode = selectedLang.getCode();

        String confirmationText = localeService.getMessage("language.changed", newLangCode);
        String menuText = localeService.getMessage("menu.idle", newLangCode);

        telegramService.sendMainMenu(session.getChatId(), confirmationText + "\n\n" + menuText, newLangCode);
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

    private void handleIdle(UserSession session) {
        String lang = session.getLanguageCode();
        telegramService.sendMainMenu(session.getChatId(), localeService.getMessage("menu.idle", lang), lang);
    }

    private boolean sendRecipe(UserSession session) {
        String lang = session.getLanguageCode();
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
            telegramService.sendRecipeMessage(session.getChatId(), text, recipe.getId(), lang);
            return true;
        } else {
            telegramService.sendMainMenu(session.getChatId(), localeService.getMessage("search.no_results", lang), lang);
            return false;
        }
    }
}
