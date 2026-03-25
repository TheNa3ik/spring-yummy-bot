package com.thena3ik.yummybot.model.enums;

import lombok.Getter;

@Getter
public enum Intolerance {
    DAIRY("menu.intolerance.dairy", "dairy"),
    EGG("menu.intolerance.egg", "egg"),
    GLUTEN("menu.intolerance.gluten", "gluten"),
    GRAIN("menu.intolerance.grain", "grain"),
    PEANUT("menu.intolerance.peanut", "peanut"),
    SEAFOOD("menu.intolerance.seafood", "seafood"),
    SESAME("menu.intolerance.sesame", "sesame"),
    SHELLFISH("menu.intolerance.shellfish", "shellfish"),
    SOY("menu.intolerance.soy", "soy"),
    SULFITE("menu.intolerance.sulfite", "sulfite"),
    TREE_NUT("menu.intolerance.tree_nut", "tree_nut"),
    WHEAT("menu.intolerance.wheat", "wheat");

    private final String labelText;
    private final String code;

    Intolerance(String labelText, String code) {
        this.labelText = labelText;
        this.code = code;
    }
}
