package com.thena3ik.mealplanner.service;

import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

@Service
public class LocaleService {
    private final MessageSource messageSource;

    private final List<String> SUPPORTED_LANGUAGES = List.of("en", "uk", "de", "fr", "es", "it", "pl", "pt");

    public LocaleService(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public String getMessage(String key, String userLangCode) {
        Locale locale = resolveLocale(userLangCode);
        return messageSource.getMessage(key, null, locale);
    }

    public String getMessage(String key, String userLangCode, Object... args) {
        Locale locale = resolveLocale(userLangCode);
        return messageSource.getMessage(key, args, locale);
    }

    private Locale resolveLocale(String langCode) {
        if (langCode == null || !SUPPORTED_LANGUAGES.contains(langCode)) {
            return Locale.ENGLISH;
        }
        return new Locale(langCode);
    }
}
