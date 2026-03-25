package com.thena3ik.yummybot.model.enums;

import lombok.Getter;

@Getter
public enum Cuisine {
    AFRICAN("menu.cuisine.african", "african"),
    ASIAN("menu.cuisine.asian", "asian"),
    AMERICAN("menu.cuisine.american", "american"),
    BRITISH("menu.cuisine.british", "british"),
    CAJUN("menu.cuisine.cajun", "cajun"),
    CARIBBEAN("menu.cuisine.caribbean", "caribbean"),
    CHINESE("menu.cuisine.chinese", "chinese"),
    EASTERN_EUROPEAN("menu.cuisine.eastern_european", "eastern european"),
    EUROPEAN("menu.cuisine.european", "european"),
    FRENCH("menu.cuisine.french", "french"),
    GERMAN("menu.cuisine.german", "german"),
    GREEK("menu.cuisine.greek", "greek"),
    INDIAN("menu.cuisine.indian", "indian"),
    IRISH("menu.cuisine.irish", "irish"),
    ITALIAN("menu.cuisine.italian", "italian"),
    JAPANESE("menu.cuisine.japanese", "japanese"),
    JEWISH("menu.cuisine.jewish", "jewish"),
    KOREAN("menu.cuisine.korean", "korean"),
    LATIN_AMERICAN("menu.cuisine.latin_american", "latin american"),
    MEDITERRANEAN("menu.cuisine.mediterranean", "mediterranean"),
    MEXICAN("menu.cuisine.mexican", "mexican"),
    MIDDLE_EASTERN("menu.cuisine.middle_eastern", "middle eastern"),
    NORDIC("menu.cuisine.nordic", "nordic"),
    SOUTHERN("menu.cuisine.southern", "southern"),
    SPANISH("menu.cuisine.spanish", "spanish"),
    THAI("menu.cuisine.thai", "thai"),
    VIETNAMESE("menu.cuisine.vietnamese", "vietnamese");

    private final String labelText;
    private final String code;

    Cuisine(String labelText, String code) {
        this.labelText = labelText;
        this.code = code;
    }
}