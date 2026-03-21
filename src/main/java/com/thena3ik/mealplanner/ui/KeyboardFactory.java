package com.thena3ik.mealplanner.ui;

import com.thena3ik.mealplanner.model.enums.Diet;
import com.thena3ik.mealplanner.model.enums.Language;
import com.thena3ik.mealplanner.service.LocaleService;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;

@Component
public class KeyboardFactory {

    private final LocaleService localeService;

    private static final String githubUrl = "https://github.com/";
    private static final String discordUrl = "https://discord.com/users/";
    private static final String donateUrl = "https://buymeacoffee.com/";

    private static final List<String> MAIN_MENU_LAYOUT = List.of(
            "main.menu.btn.search", "main.menu.btn.settings", "main.menu.btn.about");

    public KeyboardFactory(LocaleService localeService) {
        this.localeService = localeService;
    }

    public ReplyKeyboardMarkup getMainMenuKeyboard(String lang) {
        return buildKeyboardFromLayout(List.of(MAIN_MENU_LAYOUT), lang);
    }

    public ReplyKeyboardMarkup getSettingsKeyboard(String lang, boolean isAiEnabled) {
        String aiButton = isAiEnabled ? "menu.settings.btn.ai_on" : "menu.settings.btn.ai_off";

        List<List<String>> SETTINGS_MENU_LAYOUT = List.of(
                List.of(aiButton, "menu.settings.btn.diet", "menu.settings.btn.language"),
                List.of("btn.back"));

        return buildKeyboardFromLayout(SETTINGS_MENU_LAYOUT, lang);
    }

    public ReplyKeyboardMarkup getDietKeyboard(String lang, boolean isNewUser) {

        String button = isNewUser ? "btn.new" : "btn.back";

        List<List<String>> DIET_MENU_LAYOUT = List.of(
                List.of(Diet.NONE.getLabelText(), Diet.VEGETARIAN.getLabelText()),
                List.of(Diet.VEGAN.getLabelText(), Diet.KETO.getLabelText()),
                List.of(button));

        return buildKeyboardFromLayout(DIET_MENU_LAYOUT, lang);
    }

    public ReplyKeyboardMarkup getLanguageKeyboard(String lang) {
        KeyboardRow row1 = new KeyboardRow();
        row1.addAll(List.of(
                Language.ENGLISH.getDisplayText(),
                Language.UKRAINIAN.getDisplayText(),
                Language.GERMAN.getDisplayText(),
                Language.FRENCH.getDisplayText()
        ));

        KeyboardRow row2 = new KeyboardRow();
        row2.addAll(List.of(
                Language.ITALIAN.getDisplayText(),
                Language.PORTUGUESE.getDisplayText(),
                Language.SPANISH.getDisplayText(),
                Language.POLISH.getDisplayText()
        ));

        KeyboardRow row3 = new KeyboardRow();
        row3.add(localeService.getMessage("btn.back", lang));

        ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup(List.of(row1, row2, row3));
        keyboard.setResizeKeyboard(true);
        return keyboard;
    }

    private ReplyKeyboardMarkup buildKeyboardFromLayout(List<List<String>> layout, String lang) {
        List<KeyboardRow> keyboardRows = new ArrayList<>();

        for (List<String> rowKeys : layout) {
            KeyboardRow row = new KeyboardRow();
            rowKeys.forEach(key -> row.add(localeService.getMessage(key, lang)));
            keyboardRows.add(row);
        }

        ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup(keyboardRows);
        keyboard.setResizeKeyboard(true);
        return keyboard;
    }

    public InlineKeyboardMarkup getRecipeInlineButtons(int recipeId, String lang) {
        InlineKeyboardButton prevButton = InlineKeyboardButton.builder()
                .text(localeService.getMessage("flow.search.btn.prev", lang))
                .callbackData("prev")
                .build();

        InlineKeyboardButton detailsButton = InlineKeyboardButton.builder()
                .text(localeService.getMessage("flow.search.btn.details", lang))
                .callbackData("details_" + recipeId)
                .build();

        InlineKeyboardButton nextButton = InlineKeyboardButton.builder()
                .text(localeService.getMessage("flow.search.btn.next", lang))
                .callbackData("next")
                .build();

        InlineKeyboardRow row = new InlineKeyboardRow(prevButton, detailsButton, nextButton);
        return new InlineKeyboardMarkup(List.of(row));
    }

    public InlineKeyboardMarkup getAboutMeInlineButtons() {
        InlineKeyboardButton githubButton = InlineKeyboardButton.builder()
                .text("Github")
                .url(githubUrl)
                .build();

        InlineKeyboardButton discordButton = InlineKeyboardButton.builder()
                .text("Discord")
                .url(discordUrl)
                .build();

        InlineKeyboardButton donateButton = InlineKeyboardButton.builder()
                .text("Donate")
                .url(donateUrl)
                .build();

        InlineKeyboardRow row1 = new InlineKeyboardRow(githubButton, discordButton);
        InlineKeyboardRow row2 = new InlineKeyboardRow(donateButton);

        return new InlineKeyboardMarkup(List.of(row1, row2));
    }
}
