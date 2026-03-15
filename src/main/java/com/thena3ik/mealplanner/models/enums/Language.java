package com.thena3ik.mealplanner.models.enums;

import lombok.Getter;

import java.util.Optional;

@Getter
public enum Language {
    ENGLISH("🇬🇧 English", "en"),
    UKRAINIAN("🇺🇦 Українська", "uk"),
    GERMAN("🇩🇪 Deutsch", "de"),
    FRENCH("🇫🇷 Français", "fr"),
    ITALIAN("🇮🇹 Italiano", "it"),
    PORTUGUESE("🇵🇹 Português", "pt"),
    SPANISH("🇪🇸 Español", "es"),
    POLISH("🇵🇱 Polski", "pl");

    private final String displayText;
    private final String code;

    Language(String displayText, String code) {
        this.displayText = displayText;
        this.code = code;
    }

    public static Optional<Language> fromDisplayText(String text) {
        if (text == null) return Optional.empty();
        for (Language lang : values()) {
            if (lang.getDisplayText().equalsIgnoreCase(text.trim())) {
                return Optional.of(lang);
            }
        }
        return Optional.empty();
    }
}