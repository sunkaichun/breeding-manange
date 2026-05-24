package com.wens.breeding.lark.bot.event;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public final class BotMessageEventParser {
    private final ObjectMapper objectMapper;
    private final ImMessageReceiveV1EventMapper eventMapper;

    public BotMessageEventParser() {
        this(new ObjectMapper(), new ImMessageReceiveV1EventMapper());
    }

    public BotMessageEventParser(ObjectMapper objectMapper, ImMessageReceiveV1EventMapper eventMapper) {
        this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper");
        this.eventMapper = Objects.requireNonNull(eventMapper, "eventMapper");
    }

    public BotMessageEvent parseLine(String line) {
        String normalizedLine = Texts.requireText(line, "line");
        JsonNode root = readRoot(normalizedLine);
        if (!root.isObject()) {
            throw new BotMessageEventParseException("NDJSON event line must be a JSON object");
        }
        try {
            return eventMapper.map(toStringMap(root));
        } catch (IllegalArgumentException exception) {
            throw new BotMessageEventParseException("NDJSON event line does not match im.message.receive_v1 schema", exception);
        }
    }

    private JsonNode readRoot(String line) {
        try {
            return objectMapper.readTree(line);
        } catch (IOException exception) {
            throw new BotMessageEventParseException("Failed to parse NDJSON event line", exception);
        }
    }

    private static Map<String, String> toStringMap(JsonNode root) {
        Map<String, String> payload = new LinkedHashMap<>();
        root.fields().forEachRemaining(entry -> payload.put(entry.getKey(), nodeToText(entry.getValue())));
        return payload;
    }

    private static String nodeToText(JsonNode node) {
        if (node == null || node.isNull()) {
            return "";
        }
        if (node.isTextual()) {
            return node.asText();
        }
        return node.toString();
    }
}
