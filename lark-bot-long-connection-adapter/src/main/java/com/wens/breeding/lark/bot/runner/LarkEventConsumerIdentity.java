package com.wens.breeding.lark.bot.runner;

public enum LarkEventConsumerIdentity {
    BOT("bot"),
    USER("user"),
    AUTO("auto");

    private final String cliValue;

    LarkEventConsumerIdentity(String cliValue) {
        this.cliValue = cliValue;
    }

    public String getCliValue() {
        return cliValue;
    }
}
