package com.wens.breeding.lark.bot.event;

final class Texts {
    private Texts() {
    }

    static String requireText(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value;
    }
}
