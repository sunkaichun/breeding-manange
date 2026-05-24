package com.wens.breeding.graph.llm;

public final class LlmResponse {
    private final String content;
    private final String modelName;
    private final int promptTokens;
    private final int completionTokens;
    private final String finishReason;

    public LlmResponse(
            String content,
            String modelName,
            int promptTokens,
            int completionTokens,
            String finishReason) {
        this.content = requireText(content, "content");
        this.modelName = requireText(modelName, "modelName");
        this.promptTokens = requireNonNegative(promptTokens, "promptTokens");
        this.completionTokens = requireNonNegative(completionTokens, "completionTokens");
        this.finishReason = finishReason == null ? "" : finishReason;
    }

    public String getContent() {
        return content;
    }

    public String getModelName() {
        return modelName;
    }

    public int getPromptTokens() {
        return promptTokens;
    }

    public int getCompletionTokens() {
        return completionTokens;
    }

    public int getTotalTokens() {
        return promptTokens + completionTokens;
    }

    public String getFinishReason() {
        return finishReason;
    }

    private static int requireNonNegative(int value, String fieldName) {
        if (value < 0) {
            throw new IllegalArgumentException(fieldName + " must be non-negative");
        }
        return value;
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value;
    }
}
