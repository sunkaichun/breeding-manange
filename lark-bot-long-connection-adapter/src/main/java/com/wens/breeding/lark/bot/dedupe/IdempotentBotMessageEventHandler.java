package com.wens.breeding.lark.bot.dedupe;

import java.util.Objects;

import com.wens.breeding.lark.bot.event.BotMessageEvent;
import com.wens.breeding.lark.bot.event.BotMessageEventHandler;

public final class IdempotentBotMessageEventHandler implements BotMessageEventHandler {
    private final MessageDeduplicationStore store;
    private final BotMessageEventHandler delegate;

    public IdempotentBotMessageEventHandler(
            MessageDeduplicationStore store,
            BotMessageEventHandler delegate) {
        this.store = Objects.requireNonNull(store, "store");
        this.delegate = Objects.requireNonNull(delegate, "delegate");
    }

    @Override
    public void onEvent(BotMessageEvent event) {
        Objects.requireNonNull(event, "event");
        String key = MessageDeduplicationKey.from(event);
        if (!store.tryStartProcessing(key)) {
            return;
        }
        try {
            delegate.onEvent(event);
            store.markProcessed(key);
        } catch (RuntimeException exception) {
            store.markFailed(key, exception.getMessage());
            throw exception;
        }
    }
}
