package com.wens.breeding.lark.bot.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

class BotMessageEventParserTest {
    private final BotMessageEventParser parser = new BotMessageEventParser();

    @Test
    void parsesTextNdjsonLineToBotMessageEvent() {
        BotMessageEvent event = parser.parseLine(textLine());

        assertEquals("evt-001", event.getEventId());
        assertEquals("im.message.receive_v1", event.getEventType());
        assertEquals("ou_sender", event.getSenderOpenId());
        assertEquals("oc_chat", event.getChatId());
        assertEquals("om_message", event.getMessageId());
        assertEquals("text", event.getMessageType());
        assertEquals("Analyze BATCH-001 recent weight", event.getContent());
        assertTrue(event.isTextMessage());
    }

    @Test
    void keepsInteractiveContentAsRawJsonString() {
        BotMessageEvent event = parser.parseLine(interactiveLine());

        assertTrue(event.isInteractiveMessage());
        assertTrue(event.getContent().contains("card_action"));
        assertTrue(event.getContent().contains("BATCH-001"));
    }

    @Test
    void lineHandlerDispatchesParsedEvents() {
        List<BotMessageEvent> events = new ArrayList<>();
        BotMessageEventLineHandler lineHandler = BotMessageEventLineHandler.withDefaultParser(events::add);

        lineHandler.onLine(textLine());

        assertEquals(1, events.size());
        assertEquals("evt-001", events.get(0).getEventId());
    }

    @Test
    void rejectsMalformedJsonLine() {
        assertThrows(BotMessageEventParseException.class, () -> parser.parseLine("{not-json"));
    }

    @Test
    void rejectsJsonArrayLine() {
        assertThrows(BotMessageEventParseException.class, () -> parser.parseLine("[]"));
    }

    @Test
    void rejectsLineMissingRequiredField() {
        assertThrows(BotMessageEventParseException.class, () -> parser.parseLine("{\"event_id\":\"evt-001\"}"));
    }

    private static String textLine() {
        return "{"
                + "\"event_id\":\"evt-001\","
                + "\"type\":\"im.message.receive_v1\","
                + "\"timestamp\":\"1779616800000\","
                + "\"sender_id\":\"ou_sender\","
                + "\"chat_id\":\"oc_chat\","
                + "\"chat_type\":\"group\","
                + "\"message_id\":\"om_message\","
                + "\"message_type\":\"text\","
                + "\"content\":\"Analyze BATCH-001 recent weight\","
                + "\"create_time\":\"1779616799000\""
                + "}";
    }

    private static String interactiveLine() {
        return "{"
                + "\"event_id\":\"evt-002\","
                + "\"type\":\"im.message.receive_v1\","
                + "\"timestamp\":\"1779616801000\","
                + "\"sender_id\":\"ou_sender\","
                + "\"chat_id\":\"oc_chat\","
                + "\"chat_type\":\"p2p\","
                + "\"message_id\":\"om_card\","
                + "\"message_type\":\"interactive\","
                + "\"content\":\"{\\\"card_action\\\":{\\\"value\\\":{\\\"batchId\\\":\\\"BATCH-001\\\"}}}\","
                + "\"create_time\":\"1779616800000\""
                + "}";
    }
}
