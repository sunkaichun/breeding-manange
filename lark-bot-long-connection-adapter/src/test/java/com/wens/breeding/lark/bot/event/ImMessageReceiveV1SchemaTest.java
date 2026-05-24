package com.wens.breeding.lark.bot.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

class ImMessageReceiveV1SchemaTest {
    private final ImMessageReceiveV1EventMapper mapper = new ImMessageReceiveV1EventMapper();

    @Test
    void describesFlatReceiveMessageSchemaFields() {
        assertEquals("im.message.receive_v1", ImMessageReceiveV1Schema.EVENT_KEY);
        assertEquals(".", ImMessageReceiveV1Schema.JQ_ROOT_PATH);

        Map<String, EventFieldDescriptor> fields = ImMessageReceiveV1Schema.fields()
                .stream()
                .collect(Collectors.toMap(EventFieldDescriptor::getName, field -> field));

        assertEquals("open_id", fields.get("sender_id").getFormat());
        assertEquals("chat_id", fields.get("chat_id").getFormat());
        assertEquals("message_id", fields.get("message_id").getFormat());
        assertTrue(fields.get("content").getDescription().contains("interactive cards"));
    }

    @Test
    void mapsFlattenedReceiveMessagePayloadToBotMessageEvent() {
        BotMessageEvent event = mapper.map(textPayload());

        assertEquals("evt-001", event.getEventId());
        assertEquals("im.message.receive_v1", event.getEventType());
        assertEquals("ou_sender", event.getSenderOpenId());
        assertEquals("oc_chat", event.getChatId());
        assertEquals("group", event.getChatType());
        assertEquals("om_message", event.getMessageId());
        assertEquals("text", event.getMessageType());
        assertEquals("Analyze BATCH-001 recent weight", event.getContent());
        assertTrue(event.isTextMessage());
    }

    @Test
    void keepsInteractiveCardContentAsRawJsonString() {
        Map<String, String> payload = textPayload();
        payload.put("message_type", "interactive");
        payload.put("content", "{\"card_action\":{\"value\":{\"batchId\":\"BATCH-001\"}}}");

        BotMessageEvent event = mapper.map(payload);

        assertTrue(event.isInteractiveMessage());
        assertTrue(event.getContent().contains("card_action"));
    }

    @Test
    void rejectsPayloadMissingRequiredField() {
        Map<String, String> payload = textPayload();
        payload.remove("sender_id");

        assertThrows(IllegalArgumentException.class, () -> mapper.map(payload));
    }

    private static Map<String, String> textPayload() {
        Map<String, String> payload = new HashMap<>();
        payload.put("event_id", "evt-001");
        payload.put("type", "im.message.receive_v1");
        payload.put("timestamp", "1779616800000");
        payload.put("sender_id", "ou_sender");
        payload.put("chat_id", "oc_chat");
        payload.put("chat_type", "group");
        payload.put("message_id", "om_message");
        payload.put("message_type", "text");
        payload.put("content", "Analyze BATCH-001 recent weight");
        payload.put("create_time", "1779616799000");
        return payload;
    }
}
