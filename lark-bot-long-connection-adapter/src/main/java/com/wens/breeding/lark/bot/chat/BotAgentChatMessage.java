package com.wens.breeding.lark.bot.chat;

public final class BotAgentChatMessage {
    private final String role;
    private final String content;

    public BotAgentChatMessage(String role, String content) {
        this.role = requireText(role, "role");
        this.content = requireText(content, "content");
    }

    public static BotAgentChatMessage user(String content) {
        return new BotAgentChatMessage("user", content);
    }

    public String getRole() {
        return role;
    }

    public String getContent() {
        return content;
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value;
    }
}
