package com.wens.breeding.lark.bot.chat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class BotAgentChatRequest {
    private final String conversationId;
    private final List<BotAgentChatMessage> messages;
    private final boolean enableTools;

    public BotAgentChatRequest(
            String conversationId,
            List<BotAgentChatMessage> messages,
            boolean enableTools) {
        this.conversationId = requireText(conversationId, "conversationId");
        if (messages == null || messages.isEmpty()) {
            throw new IllegalArgumentException("messages must not be empty");
        }
        this.messages = Collections.unmodifiableList(new ArrayList<>(messages));
        this.enableTools = enableTools;
    }

    public static BotAgentChatRequest singleUserMessage(String conversationId, String content) {
        return new BotAgentChatRequest(conversationId, List.of(BotAgentChatMessage.user(content)), true);
    }

    public String getConversationId() {
        return conversationId;
    }

    public List<BotAgentChatMessage> getMessages() {
        return messages;
    }

    public boolean isEnableTools() {
        return enableTools;
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value;
    }
}
