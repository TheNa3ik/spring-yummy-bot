package com.thena3ik.yummybot.ui;

import com.thena3ik.yummybot.model.entity.UserEntity;
import com.thena3ik.yummybot.model.enums.Diet;
import com.thena3ik.yummybot.model.enums.Language;
import com.thena3ik.yummybot.service.LocaleService;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;

@Component
public class ReplyKeyboardFactory {

    private final LocaleService localeService;

    private static final List<List<String>> MAIN_MENU_LAYOUT = List.of(
            List.of("main.menu.btn.search"),
            List.of("main.menu.btn.settings", "main.menu.btn.about")
    );

    private static final List<List<String>> SEARCH_MENU_LAYOUT = List.of(
            List.of("menu.search.btn.ingredients", "menu.search.btn.name"),
            List.of("menu.search.btn.random", "btn.back")
    );

    public ReplyKeyboardFactory(LocaleService localeService) {
        this.localeService = localeService;
    }

    public ReplyKeyboardMarkup getMainMenuKeyboard(UserEntity session) {
        return buildReplyKeyboard(MAIN_MENU_LAYOUT, session.getLanguageCode());
    }

    public ReplyKeyboardMarkup getSearchMenuKeyboard(UserEntity session) {
        return buildReplyKeyboard(SEARCH_MENU_LAYOUT, session.getLanguageCode(), true);
    }

    public ReplyKeyboardMarkup getRecipeMenuKeyboard(UserEntity session) {
        return buildReplyKeyboard(List.of(List.of("btn.back")), session.getLanguageCode());
    }

    public ReplyKeyboardMarkup getSettingsMenuKeyboard(UserEntity session) {
        String lang = session.getLanguageCode();

        String aiButton = session.isAiTranslationEnabled() ? "menu.settings.btn.ai_on" : "menu.settings.btn.ai_off";

        List<List<String>> layout = List.of(
                List.of(aiButton, "menu.settings.btn.diet"),
                List.of("menu.settings.btn.language", "menu.settings.btn.intolerances"),
                List.of("btn.back")
        );

        return buildReplyKeyboard(layout, lang);
    }

    public ReplyKeyboardMarkup getDietMenuKeyboard(UserEntity session) {
        boolean isNewUser = session.getDiet() == null;
        String lang = session.getLanguageCode();
        String backOrNewButton = isNewUser ? "btn.new" : "btn.back";

        List<List<String>> layout = List.of(
                List.of(Diet.NONE.getLabelText(), Diet.VEGETARIAN.getLabelText()),
                List.of(Diet.VEGAN.getLabelText(), Diet.KETO.getLabelText()),
                List.of(backOrNewButton)
        );

        return buildReplyKeyboard(layout, lang);
    }

    public ReplyKeyboardMarkup getLanguageMenuKeyboard(UserEntity session) {
        String lang = session.getLanguageCode();

        List<List<String>> layout = List.of(
                List.of(Language.ENGLISH.getDisplayText(), Language.UKRAINIAN.getDisplayText()),
                List.of(Language.GERMAN.getDisplayText(), Language.FRENCH.getDisplayText()),
                List.of(Language.ITALIAN.getDisplayText(), Language.PORTUGUESE.getDisplayText()),
                List.of(Language.SPANISH.getDisplayText(), Language.POLISH.getDisplayText()),

                List.of(localeService.getMessage("btn.back", lang))
        );

        return buildRawReplyKeyboard(layout);
    }

    private ReplyKeyboardMarkup buildReplyKeyboard(List<List<String>> layoutKeys, String lang) {
        return buildReplyKeyboard(layoutKeys, lang, false);
    }

    private ReplyKeyboardMarkup buildReplyKeyboard(List<List<String>> layoutKeys, String lang, boolean isOneTimeKeyboard) {
        List<KeyboardRow> keyboardRows = new ArrayList<>();

        for (List<String> rowKeys : layoutKeys) {
            KeyboardRow row = new KeyboardRow();
            rowKeys.forEach(key -> row.add(localeService.getMessage(key, lang)));
            keyboardRows.add(row);
        }

        ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup(keyboardRows);
        keyboard.setResizeKeyboard(true);
        keyboard.setOneTimeKeyboard(isOneTimeKeyboard);
        return keyboard;
    }

    private ReplyKeyboardMarkup buildRawReplyKeyboard(List<List<String>> rawTextLayout) {
        List<KeyboardRow> keyboardRows = new ArrayList<>();

        for (List<String> rowText : rawTextLayout) {
            KeyboardRow row = new KeyboardRow();
            row.addAll(rowText);
            keyboardRows.add(row);
        }

        ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup(keyboardRows);
        keyboard.setResizeKeyboard(true);
        return keyboard;
    }
}