package com.thena3ik.mealplanner.service;

import com.thena3ik.mealplanner.models.Diet;
import com.thena3ik.mealplanner.models.Language;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.List;

@Service
public class TelegramService {
    private final TelegramClient telegramClient;
    private final LocaleService localeService;

    @Autowired
    public TelegramService(TelegramClient telegramClient, LocaleService localeService) {
        this.telegramClient = telegramClient;
        this.localeService = localeService;
    }

    public void sendMessage(Long chatId, String text) {
        sendMessage(chatId, text, null);
    }

    public void sendMessage(Long chatId, String text, ReplyKeyboardMarkup keyboard) {
        SendMessage message = SendMessage.builder()
                .chatId(chatId)
                .text(text)
                .replyMarkup(keyboard)
                .parseMode("Markdown")
                .build();
        try {
            telegramClient.execute(message);
        } catch (TelegramApiException e) {
            System.out.println("SendMessage error: " + e.getMessage());
        }
    }

    private ReplyKeyboardMarkup mainMenuKeyboard(String lang) {
        KeyboardRow row = new KeyboardRow();
        row.add(new KeyboardButton(localeService.getMessage("menu.button.search", lang)));
        row.add(new KeyboardButton(localeService.getMessage("menu.button.diet", lang)));
        row.add(new KeyboardButton(localeService.getMessage("menu.button.language", lang)));

        ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup(List.of(row));
        keyboard.setResizeKeyboard(true);
        keyboard.setOneTimeKeyboard(false);
        return keyboard;
    }

    private ReplyKeyboardMarkup dietKeyboard(String lang) {
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

    public void sendMainMenu(Long chatId, String text, String lang) {
        sendMessage(chatId, text, mainMenuKeyboard(lang));
    }

    public void sendDietKeyboard(Long chatId, String text, String lang) {
        sendMessage(chatId, text, dietKeyboard(lang));
    }

    public InlineKeyboardMarkup recipeInlineButtons (int recipeId, String lang) {
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

    public void sendRecipeMessage (Long chatId, String text, int recipeId, String lang) {
        InlineKeyboardMarkup keyboard = recipeInlineButtons(recipeId, lang);

        SendMessage message = SendMessage.builder()
                .chatId(chatId)
                .text(text)
                .replyMarkup(keyboard)
                .parseMode("Markdown")
                .build();
        try {
            telegramClient.execute(message);
        } catch (TelegramApiException e) {
            System.out.println("SendMessage error: " + e.getMessage());
        }
    }

    public void answerCallback(String callbackId, String text) {
        AnswerCallbackQuery answer = AnswerCallbackQuery.builder()
                .callbackQueryId(callbackId)
                .text(text)
                .showAlert(false)
                .build();

        try {
            telegramClient.execute(answer);
        } catch (TelegramApiException e) {
            System.err.println("AnswerCallback error: " + e.getMessage());
        }
    }

    public void editMessage(Long chatId, Integer messageId, String newText, InlineKeyboardMarkup keyboard) {
        EditMessageText edit = EditMessageText.builder()
                .chatId(chatId)
                .messageId(messageId)
                .text(newText)
                .replyMarkup(keyboard)
                .parseMode("Markdown")
                .build();

        try {
            telegramClient.execute(edit);
        } catch (TelegramApiException e) {
            System.err.println("EditMessage error: " + e.getMessage());
        }
    }

    private ReplyKeyboardMarkup languageKeyboard() {
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

    public void sendLanguageMenu(Long chatId, String text) { sendMessage(chatId, text, languageKeyboard()); }

}