package com.wens.breeding.graph.llm;

import java.util.Objects;

public final class StaticJsonLlmGateway implements LlmGateway {
    private final String jsonContent;
    private final String modelName;

    public StaticJsonLlmGateway(String jsonContent) {
        this(jsonContent, "static-json-test-model");
    }

    public StaticJsonLlmGateway(String jsonContent, String modelName) {
        this.jsonContent = requireJsonObject(jsonContent);
        this.modelName = requireText(modelName, "modelName");
    }

    @Override
    public LlmResponse complete(LlmRequest request) {
        Objects.requireNonNull(request, "request");
        return new LlmResponse(
                jsonContent,
                modelName,
                estimateTokens(request.getSystemPrompt()) + estimateTokens(request.getUserPrompt()),
                estimateTokens(jsonContent),
                "stop");
    }

    private static String requireJsonObject(String value) {
        String text = requireText(value, "jsonContent").trim();
        if (!text.startsWith("{") || !text.endsWith("}")) {
            throw new IllegalArgumentException("jsonContent must be a JSON object string");
        }
        return text;
    }

    private static int estimateTokens(String text) {
        return Math.max(1, text.length() / 4);
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value;
    }
}
