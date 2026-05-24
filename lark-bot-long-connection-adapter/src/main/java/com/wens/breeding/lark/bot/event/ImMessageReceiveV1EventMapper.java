package com.wens.breeding.lark.bot.event;

import java.util.Map;
import java.util.Objects;

public final class ImMessageReceiveV1EventMapper {
    public BotMessageEvent map(Map<String, String> payload) {
        Map<String, String> nonNullPayload = Objects.requireNonNull(payload, "payload");
        return new BotMessageEvent(
                required(nonNullPayload, ImMessageReceiveV1Schema.FIELD_EVENT_ID),
                required(nonNullPayload, ImMessageReceiveV1Schema.FIELD_EVENT_TYPE),
                required(nonNullPayload, ImMessageReceiveV1Schema.FIELD_SENDER_ID),
                required(nonNullPayload, ImMessageReceiveV1Schema.FIELD_CHAT_ID),
                required(nonNullPayload, ImMessageReceiveV1Schema.FIELD_CHAT_TYPE),
                required(nonNullPayload, ImMessageReceiveV1Schema.FIELD_MESSAGE_ID),
                required(nonNullPayload, ImMessageReceiveV1Schema.FIELD_MESSAGE_TYPE),
                required(nonNullPayload, ImMessageReceiveV1Schema.FIELD_CONTENT),
                required(nonNullPayload, ImMessageReceiveV1Schema.FIELD_CREATE_TIME),
                optional(nonNullPayload, ImMessageReceiveV1Schema.FIELD_DELIVERY_TIMESTAMP));
    }

    private static String required(Map<String, String> payload, String fieldName) {
        return Texts.requireText(payload.get(fieldName), fieldName);
    }

    private static String optional(Map<String, String> payload, String fieldName) {
        String value = payload.get(fieldName);
        return value == null ? "" : value;
    }
}
