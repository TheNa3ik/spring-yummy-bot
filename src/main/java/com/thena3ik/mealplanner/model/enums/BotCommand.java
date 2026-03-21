package com.thena3ik.mealplanner.model.enums;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum BotCommand {
    START("/start"),
    CANCEL("/cancel");

    private final String value;

    BotCommand(String value) {
        this.value = value;
    }

    public static BotCommand fromText(String text) {
        return Arrays.stream(values())
                .filter(cmd -> cmd.value.equalsIgnoreCase(text))
                .findFirst()
                .orElse(null);
    }
}
