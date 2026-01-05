package com.thena3ik.mealplanner.service;

import com.thena3ik.mealplanner.models.Diet;
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

    @Autowired
    public TelegramService(TelegramClient telegramClient) {
        this.telegramClient = telegramClient;
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

    private ReplyKeyboardMarkup mainMenuKeyboard() {
        KeyboardRow row = new KeyboardRow();
        row.add(new KeyboardButton("🍽 Search"));
        row.add(new KeyboardButton("\uD83C\uDF5C Diet preferences"));
        ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup(List.of(row));
        keyboard.setResizeKeyboard(true);
        keyboard.setOneTimeKeyboard(false);
        return keyboard;
    }

    private ReplyKeyboardMarkup dietKeyboard() {
        KeyboardRow row1 = new KeyboardRow();
        row1.add(new KeyboardButton(Diet.NONE.getDisplayText()));
        row1.add(new KeyboardButton(Diet.VEGETARIAN.getDisplayText()));

        KeyboardRow row2 = new KeyboardRow();
        row2.add(new KeyboardButton(Diet.VEGAN.getDisplayText()));
        row2.add(new KeyboardButton(Diet.KETO.getDisplayText()));

        ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup(List.of(row1, row2));
        keyboard.setResizeKeyboard(true);
        keyboard.setOneTimeKeyboard(true);
        return keyboard;
    }

    public void sendMainMenu(Long chatId, String text) {
        sendMessage(chatId, text, mainMenuKeyboard());
    }

    public void sendDietKeyboard(Long chatId, String text) {
        sendMessage(chatId, text, dietKeyboard());
    }

    public boolean isValidDietOption(String text) {
        return Diet.fromDisplayText(text).isPresent();
    }

    public InlineKeyboardMarkup recipeInlineButtons (int recipeId) {
        InlineKeyboardButton prevButton = InlineKeyboardButton.builder()
                .text("⬅️ Previous")
                .callbackData("prev")
                .build();

        InlineKeyboardButton detailsButton = InlineKeyboardButton.builder()
                .text("📖 View Details")
                .callbackData("details_" + recipeId)
                .build();

        InlineKeyboardButton nextButton = InlineKeyboardButton.builder()
                .text("➡️ Next")
                .callbackData("next")
                .build();

        InlineKeyboardRow row = new InlineKeyboardRow();
        row.addAll(List.of(prevButton, detailsButton, nextButton));

        return new InlineKeyboardMarkup(List.of(row));
    }

    public void sendRecipeMessage (Long chatId, String text, int recipeId) {
        InlineKeyboardMarkup keyboard = recipeInlineButtons(recipeId);

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

}