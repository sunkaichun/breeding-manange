package com.wens.breeding.lark.bot.event;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class ImMessageReceiveV1Schema {
    public static final String EVENT_KEY = "im.message.receive_v1";
    public static final String JQ_ROOT_PATH = ".";

    public static final String FIELD_EVENT_ID = "event_id";
    public static final String FIELD_EVENT_TYPE = "type";
    public static final String FIELD_DELIVERY_TIMESTAMP = "timestamp";
    public static final String FIELD_SENDER_ID = "sender_id";
    public static final String FIELD_CHAT_ID = "chat_id";
    public static final String FIELD_CHAT_TYPE = "chat_type";
    public static final String FIELD_MESSAGE_ID = "message_id";
    public static final String FIELD_MESSAGE_TYPE = "message_type";
    public static final String FIELD_CONTENT = "content";
    public static final String FIELD_CREATE_TIME = "create_time";

    private static final List<EventFieldDescriptor> FIELDS = Collections.unmodifiableList(Arrays.asList(
            new EventFieldDescriptor(FIELD_EVENT_ID, "", "Globally unique event ID; safe for deduplication"),
            new EventFieldDescriptor(FIELD_EVENT_TYPE, "", "Event type; always im.message.receive_v1"),
            new EventFieldDescriptor(FIELD_DELIVERY_TIMESTAMP, "timestamp_ms", "Event delivery time in milliseconds"),
            new EventFieldDescriptor(FIELD_SENDER_ID, "open_id", "Sender open_id; prefixed with ou_"),
            new EventFieldDescriptor(FIELD_CHAT_ID, "chat_id", "Chat ID; prefixed with oc_"),
            new EventFieldDescriptor(FIELD_CHAT_TYPE, "", "Conversation type, such as p2p or group"),
            new EventFieldDescriptor(FIELD_MESSAGE_ID, "message_id", "Message ID; prefixed with om_"),
            new EventFieldDescriptor(FIELD_MESSAGE_TYPE, "", "Message type, such as text or interactive"),
            new EventFieldDescriptor(
                    FIELD_CONTENT,
                    "",
                    "Pre-rendered human-readable content except interactive cards, which keep raw JSON"),
            new EventFieldDescriptor(FIELD_CREATE_TIME, "timestamp_ms", "Message creation time in milliseconds")));

    private static final List<String> REQUIRED_FIELDS = Collections.unmodifiableList(Arrays.asList(
            FIELD_EVENT_ID,
            FIELD_EVENT_TYPE,
            FIELD_SENDER_ID,
            FIELD_CHAT_ID,
            FIELD_CHAT_TYPE,
            FIELD_MESSAGE_ID,
            FIELD_MESSAGE_TYPE,
            FIELD_CONTENT,
            FIELD_CREATE_TIME));

    private ImMessageReceiveV1Schema() {
    }

    public static List<EventFieldDescriptor> fields() {
        return FIELDS;
    }

    public static List<String> requiredFields() {
        return REQUIRED_FIELDS;
    }
}
