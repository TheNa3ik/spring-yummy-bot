package com.thena3ik.mealplanner.components;

import com.thena3ik.mealplanner.models.enums.Diet;
import com.thena3ik.mealplanner.models.enums.Language;
import com.thena3ik.mealplanner.service.LocaleService;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.List;

@Component
public class KeyboardFactory {

    private final LocaleService localeService;

    public KeyboardFactory(LocaleService localeService) {
        this.localeService = localeService;
    }

    public ReplyKeyboardMarkup getMainMenuKeyboard(String lang) {
        KeyboardRow row = new KeyboardRow();
        row.add(new KeyboardButton(localeService.getMessage("menu.button.search", lang)));
        row.add(new KeyboardButton(localeService.getMessage("menu.button.diet", lang)));
        row.add(new KeyboardButton(localeService.getMessage("menu.button.language", lang)));

        ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup(List.of(row));
        keyboard.setResizeKeyboard(true);
        keyboard.setOneTimeKeyboard(false);
        return keyboard;
    }

    public ReplyKeyboardMarkup getDietKeyboard(String lang) {
        KeyboardRow row1 = new KeyboardRow();
        row1.add(new KeyboardButton(localeService.getMessage(Diet.NONE.getLabelKey(), lang)));
        row1.add(new KeyboardButton(localeService.getMessage(Diet.VEGETARIAN.getLabelKey(), lang)));

        KeyboardRow row2 = new KeyboardRow();
        row2.add(new KeyboardButton(localeService.getMessage(Diet.VEGAN.getLabelKey(), lang)));
        row2.add(new KeyboardButton(localeService.getMessage(Diet.KETO.getLabelKey(), lang)));

        ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup(List.of(row1, row2));
        keyboard.setResizeKeyboard(true);
        keyboard.setOneTimeKeyboard(true);
        return keyboard;
    }

    public ReplyKeyboardMarkup getLanguageKeyboard() {
        KeyboardRow row1 = new KeyboardRow();
        row1.add(new KeyboardButton(Language.ENGLISH.getDisplayText()));
        row1.add(new KeyboardButton(Language.UKRAINIAN.getDisplayText()));
        row1.add(new KeyboardButton(Language.GERMAN.getDisplayText()));
        row1.add(new KeyboardButton(Language.FRENCH.getDisplayText()));

        KeyboardRow row2 = new KeyboardRow();
        row2.add(new KeyboardButton(Language.POLISH.getDisplayText()));
        row2.add(new KeyboardButton(Language.PORTUGUESE.getDisplayText()));
        row2.add(new KeyboardButton(Language.SPANISH.getDisplayText()));
        row2.add(new KeyboardButton(Language.ITALIAN.getDisplayText()));

        ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup(List.of(row1, row2));
        keyboard.setResizeKeyboard(true);
        keyboard.setOneTimeKeyboard(true);
        return keyboard;
    }

    public InlineKeyboardMarkup getRecipeInlineButtons(int recipeId, String lang) {
        InlineKeyboardButton prevButton = InlineKeyboardButton.builder()
                .text(localeService.getMessage("search.button.previous", lang))
                .callbackData("prev")
                .build();

        InlineKeyboardButton detailsButton = InlineKeyboardButton.builder()
                .text(localeService.getMessage("search.button.details", lang))
                .callbackData("details_" + recipeId)
                .build();

        InlineKeyboardButton nextButton = InlineKeyboardButton.builder()
                .text(localeService.getMessage("search.button.next", lang))
                .callbackData("next")
                .build();

        InlineKeyboardRow row = new InlineKeyboardRow();
        row.addAll(List.of(prevButton, detailsButton, nextButton));

        return new InlineKeyboardMarkup(List.of(row));
    }
}
