package com.thena3ik.mealplanner.service;

import com.thena3ik.mealplanner.components.KeyboardFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Slf4j
@Service
public class TelegramService {

    private final TelegramClient telegramClient;
    private final KeyboardFactory keyboardFactory;

    @Autowired
    public TelegramService(TelegramClient telegramClient, KeyboardFactory keyboardFactory) {
        this.telegramClient = telegramClient;
        this.keyboardFactory = keyboardFactory;
    }

    public void sendMessage(Long chatId, String text) {
        sendMessage(chatId, text, null);
    }

    public void sendMessage(Long chatId, String text, ReplyKeyboard keyboard) {
        SendMessage message = SendMessage.builder()
                .chatId(chatId)
                .text(text)
                .replyMarkup(keyboard)
                .parseMode("Markdown")
                .build();
        try {
            telegramClient.execute(message);
        } catch (TelegramApiException e) {
            log.error("Failed to send message to chat ID: {}", chatId, e);
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
            log.error("Failed to edit message ID: {} for chat ID: {}", messageId, chatId, e);
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
            log.error("Failed to answer callback ID: {}", callbackId, e);
        }
    }

    public void sendMainMenu(Long chatId, String text, String lang) {
        sendMessage(chatId, text, keyboardFactory.getMainMenuKeyboard(lang));
    }

    public void sendDietKeyboard(Long chatId, String text, String lang) {
        sendMessage(chatId, text, keyboardFactory.getDietKeyboard(lang));
    }

    public void sendLanguageMenu(Long chatId, String text) {
        sendMessage(chatId, text, keyboardFactory.getLanguageKeyboard());
    }

    public void sendRecipeMessage (Long chatId, String text, int recipeId, String lang) {
        sendMessage(chatId, text, keyboardFactory.getRecipeInlineButtons(recipeId, lang));
    }

    public InlineKeyboardMarkup recipeInlineButtons(int recipeId, String lang) {
        return keyboardFactory.getRecipeInlineButtons(recipeId, lang);
    }
}