package com.thena3ik.yummybot.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

@Slf4j
@Service
public class LocaleService {

    private final MessageSource messageSource;
    private final List<String> SUPPORTED_LANGUAGES = List.of("en", "uk", "de", "fr", "es", "it", "pl", "pt");

    public LocaleService(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public String getMessage(String key, String userLangCode) {
        try {
            Locale locale = resolveLocale(userLangCode);
            return messageSource.getMessage(key, null, locale);
        } catch (NoSuchMessageException e) {
            log.error("Translation key not found: '{}' for locale '{}'", key, userLangCode);
            return key;
        }
    }

    public String getMessage(String key, String userLangCode, Object... args) {
        try {
            Locale locale = resolveLocale(userLangCode);
            return messageSource.getMessage(key, args, locale);
        } catch (NoSuchMessageException e) {
            log.error("Translation key not found (with args): '{}' for locale '{}'", key, userLangCode);
            return key;
        }
    }

    private Locale resolveLocale(String langCode) {
        if (langCode == null || !SUPPORTED_LANGUAGES.contains(langCode)) {
            log.warn("Unsupported or null language code provided: '{}'. Defaulting to English.", langCode);
            return Locale.ENGLISH;
        }
        return Locale.of(langCode);
    }
}
