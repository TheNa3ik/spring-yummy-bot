package com.thena3ik.yummybot.ui;

import com.thena3ik.yummybot.model.entity.UserEntity;
import com.thena3ik.yummybot.model.enums.Cuisine;
import com.thena3ik.yummybot.model.enums.Intolerance;
import com.thena3ik.yummybot.service.LocaleService;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import java.util.ArrayList;
import java.util.List;

@Component
public class InlineKeyboardFactory {

    private final LocaleService localeService;

    private static final String GITHUB_URL = "https://github.com/";
    private static final String DISCORD_URL = "https://discord.com/users/";
    private static final String DONATE_URL = "https://buymeacoffee.com/";

    private static final List<List<Intolerance>> INTOLERANCES_MENU_LAYOUT = List.of(
            List.of(Intolerance.DAIRY, Intolerance.EGG, Intolerance.GLUTEN),
            List.of(Intolerance.GRAIN, Intolerance.PEANUT, Intolerance.SEAFOOD),
            List.of(Intolerance.SESAME, Intolerance.SHELLFISH, Intolerance.SOY),
            List.of(Intolerance.SULFITE, Intolerance.TREE_NUT, Intolerance.WHEAT)
    );

    public InlineKeyboardFactory(LocaleService localeService) {
        this.localeService = localeService;
    }

    public InlineKeyboardMarkup getCancelKeyboard(UserEntity session, String callbackName) {
        String lang = session.getLanguageCode();
        List<InlineKeyboardRow> rows = new ArrayList<>();
        InlineKeyboardRow row = new InlineKeyboardRow();

        String suffix = (callbackName != null && !callbackName.isEmpty()) ? "_" + callbackName : "";

        row.add(InlineKeyboardButton.builder()
                .text(localeService.getMessage("btn.cancel", lang))
                .callbackData("cancel" + suffix)
                .build());

        rows.add(row);
        return new InlineKeyboardMarkup(rows);
    }

    public InlineKeyboardMarkup getRecipeKeyboard(UserEntity session, int recipeId) {
        List<InlineKeyboardRow> rows = new ArrayList<>();
        InlineKeyboardRow row = new InlineKeyboardRow();

        String lang = session.getLanguageCode();

        row.add(InlineKeyboardButton.builder()
                .text(localeService.getMessage("flow.search.btn.prev", lang))
                .callbackData("recipe_prev")
                .build());

        row.add(InlineKeyboardButton.builder()
                .text(localeService.getMessage("flow.search.btn.details", lang))
                .callbackData("recipe_details_" + recipeId)
                .build());

        row.add(InlineKeyboardButton.builder()
                .text(localeService.getMessage("flow.search.btn.next", lang))
                .callbackData("recipe_next")
                .build());

        rows.add(row);
        return new InlineKeyboardMarkup(rows);
    }

    public InlineKeyboardMarkup getAboutKeyboard() {
        List<InlineKeyboardRow> rows = new ArrayList<>();

        InlineKeyboardRow row1 = new InlineKeyboardRow(
                InlineKeyboardButton.builder().text("Github").url(GITHUB_URL).build(),
                InlineKeyboardButton.builder().text("Discord").url(DISCORD_URL).build()
        );

        InlineKeyboardRow row2 = new InlineKeyboardRow(
                InlineKeyboardButton.builder().text("Donate").url(DONATE_URL).build()
        );

        rows.add(row1);
        rows.add(row2);
        return new InlineKeyboardMarkup(rows);
    }

    public InlineKeyboardMarkup getIntoleranceKeyboard(UserEntity session) {
        List<InlineKeyboardRow> rows = new ArrayList<>();
        String lang = session.getLanguageCode();
        String activeIntolerances = session.getIntolerances() != null ? session.getIntolerances() : "";

        for (List<Intolerance> rowEnums : INTOLERANCES_MENU_LAYOUT) {
            InlineKeyboardRow row = new InlineKeyboardRow();

            for (Intolerance intolerance : rowEnums) {
                String buttonText = localeService.getMessage(intolerance.getLabelText(), lang);

                if (activeIntolerances.contains(intolerance.getCode())) {
                    buttonText = "🟢 " + buttonText;
                }

                row.add(InlineKeyboardButton.builder()
                        .text(buttonText)
                        .callbackData("intol_" + intolerance.getCode())
                        .build());
            }
            rows.add(row);
        }

        InlineKeyboardRow saveRow = new InlineKeyboardRow();
        saveRow.add(InlineKeyboardButton.builder()
                .text(localeService.getMessage("btn.save", lang))
                .callbackData("intol_save")
                .build());

        rows.add(saveRow);
        return new InlineKeyboardMarkup(rows);
    }

    public InlineKeyboardMarkup getCuisineKeyboard(UserEntity session, int page) {
        String lang = session.getLanguageCode();
        List<InlineKeyboardRow> rows = new ArrayList<>();
        List<Cuisine> allCuisines = List.of(Cuisine.values());

        int itemsPerPage = 8;
        int totalPages = (int) Math.ceil((double) allCuisines.size() / itemsPerPage);

        page = Math.max(1, Math.min(page, totalPages));

        int startIndex = (page - 1) * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, allCuisines.size());

        List<Cuisine> cuisinesForThisPage = allCuisines.subList(startIndex, endIndex);

        InlineKeyboardRow currentRow = new InlineKeyboardRow();
        for (int i = 0; i < cuisinesForThisPage.size(); i++) {
            Cuisine cuisine = cuisinesForThisPage.get(i);
            currentRow.add(InlineKeyboardButton.builder()
                    .text(localeService.getMessage(cuisine.getLabelText(), lang))
                    .callbackData("cuisine_set_" + cuisine.name())
                    .build());

            if (currentRow.size() == 2 || i == cuisinesForThisPage.size() - 1) {
                rows.add(currentRow);
                currentRow = new InlineKeyboardRow();
            }
        }

        if (totalPages > 1) {
            InlineKeyboardRow navRow = new InlineKeyboardRow();
            for (int p = 1; p <= totalPages; p++) {
                String buttonText = (p == page) ? "-" + p + "-" : String.valueOf(p);
                String callbackData = (p == page) ? "cuisine_ignore" : "cuisine_page_" + p;

                navRow.add(InlineKeyboardButton.builder()
                        .text(buttonText)
                        .callbackData(callbackData)
                        .build());
            }
            rows.add(navRow);
        }

        InlineKeyboardRow actionRow = new InlineKeyboardRow(
                InlineKeyboardButton.builder()
                        .text(localeService.getMessage("menu.cuisine.btn.skip", lang))
                        .callbackData("cuisine_skip")
                        .build(),
                InlineKeyboardButton.builder()
                        .text(localeService.getMessage("btn.cancel", lang))
                        .callbackData("cancel")
                        .build()
        );

        rows.add(actionRow);
        return new InlineKeyboardMarkup(rows);
    }
}