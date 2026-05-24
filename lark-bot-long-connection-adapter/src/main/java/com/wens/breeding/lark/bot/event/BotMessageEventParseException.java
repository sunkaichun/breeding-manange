package com.wens.breeding.lark.bot.event;

public class BotMessageEventParseException extends RuntimeException {
    public BotMessageEventParseException(String message) {
        super(message);
    }

    public BotMessageEventParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
