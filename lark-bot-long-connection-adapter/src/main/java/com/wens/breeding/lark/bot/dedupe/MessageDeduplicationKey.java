package com.wens.breeding.lark.bot.dedupe;

import com.wens.breeding.lark.bot.event.BotMessageEvent;

public final class MessageDeduplicationKey {
    private MessageDeduplicationKey() {
    }

    public static String from(BotMessageEvent event) {
        if (event.getEventId() != null && !event.getEventId().trim().isEmpty()) {
            return "event:" + event.getEventId();
        }
        return "message:" + Texts.requireText(event.getMessageId(), "messageId");
    }
}
