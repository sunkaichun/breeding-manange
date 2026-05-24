package com.wens.breeding.lark.bot.event;

import java.util.Objects;

import com.wens.breeding.lark.bot.runner.EventLineHandler;

public final class BotMessageEventLineHandler implements EventLineHandler {
    private final BotMessageEventParser parser;
    private final BotMessageEventHandler handler;

    public BotMessageEventLineHandler(BotMessageEventParser parser, BotMessageEventHandler handler) {
        this.parser = Objects.requireNonNull(parser, "parser");
        this.handler = Objects.requireNonNull(handler, "handler");
    }

    public static BotMessageEventLineHandler withDefaultParser(BotMessageEventHandler handler) {
        return new BotMessageEventLineHandler(new BotMessageEventParser(), handler);
    }

    @Override
    public void onLine(String line) {
        handler.onEvent(parser.parseLine(line));
    }
}
