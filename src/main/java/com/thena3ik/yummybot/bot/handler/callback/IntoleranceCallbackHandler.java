package com.thena3ik.yummybot.bot.handler.callback;

import com.thena3ik.yummybot.model.entity.UserEntity;
import com.thena3ik.yummybot.model.enums.Intolerance;
import com.thena3ik.yummybot.service.LocaleService;
import com.thena3ik.yummybot.service.TelegramService;
import com.thena3ik.yummybot.service.UserService;
import com.thena3ik.yummybot.ui.InlineKeyboardFactory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class IntoleranceCallbackHandler implements CallbackHandler {

    private final UserService userService;
    private final TelegramService telegramService;
    private final LocaleService localeService;
    private final InlineKeyboardFactory inlineFactory;

    public IntoleranceCallbackHandler(UserService userService,
                                      TelegramService telegramService,
                                      LocaleService localeService,
                                      InlineKeyboardFactory inlineFactory) {
        this.userService = userService;
        this.telegramService = telegramService;
        this.localeService = localeService;
        this.inlineFactory = inlineFactory;
    }

    @Override
    public String getSupportedPrefix() {
        return "intol_";
    }

    @Override
    public void handle(UserEntity session, CallbackQuery callback) {
        String data = callback.getData();
        String callbackId = callback.getId();
        long chatId = session.getChatId();
        int messageId = callback.getMessage().getMessageId();
        String lang = session.getLanguageCode();

        if (data.equals("intol_save")) {
            String currentStr = session.getIntolerances() != null ? session.getIntolerances() : "";
            String finalText;

            if (currentStr.isEmpty()) {
                finalText = localeService.getMessage("menu.intolerances.saved.none", lang);
            } else {
                String translatedList = Arrays.stream(currentStr.split(","))
                        .filter(s -> !s.isBlank())
                        .map(code -> {
                            Intolerance intolerance = Intolerance.valueOf(code.toUpperCase());
                            return "🟢 " + localeService.getMessage(intolerance.getLabelText(), lang);
                        })
                        .collect(Collectors.joining("\n"));

                finalText = localeService.getMessage("menu.intolerances.saved.list", lang, translatedList);
            }

            telegramService.editMessage(chatId, messageId, finalText, null);
            telegramService.answerCallback(callbackId, null);
            return;
        }

        Set<String> activeIntolerances = getUpdatedIntolerances(session, data);
        session.setIntolerances(String.join(",", activeIntolerances));
        userService.save(session);

        InlineKeyboardMarkup keyboard = inlineFactory.getIntoleranceKeyboard(session);
        telegramService.editReplyMarkup(chatId, messageId, keyboard);
        telegramService.answerCallback(callbackId, null);
    }

    private Set<String> getUpdatedIntolerances(UserEntity session, String data) {
        String clickedIntolerance = data.replace(getSupportedPrefix(), "");
        String currentStr = session.getIntolerances() != null ? session.getIntolerances() : "";

        Set<String> activeSet = new HashSet<>(Arrays.asList(currentStr.split(",")));
        activeSet.remove("");

        if (activeSet.contains(clickedIntolerance)) {
            activeSet.remove(clickedIntolerance);
        } else {
            activeSet.add(clickedIntolerance);
        }
        return activeSet;
    }
}
