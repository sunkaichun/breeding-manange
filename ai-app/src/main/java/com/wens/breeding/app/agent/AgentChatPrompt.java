package com.wens.breeding.app.agent;

public final class AgentChatPrompt {
    private final String systemPrompt;
    private final String userPrompt;

    public AgentChatPrompt(String systemPrompt, String userPrompt) {
        this.systemPrompt = requireText(systemPrompt, "systemPrompt");
        this.userPrompt = requireText(userPrompt, "userPrompt");
    }

    public String getSystemPrompt() {
        return systemPrompt;
    }

    public String getUserPrompt() {
        return userPrompt;
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value;
    }
}
