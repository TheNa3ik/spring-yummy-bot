package com.thena3ik.mealplanner.bot.commands;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum BotCommand {
    START("/start"),
    SEARCH("🍽 Search"),
    DIET_PREFERENCES("\uD83C\uDF5C Diet preferences");

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
