package com.wens.breeding.app.agent;

import java.util.List;

public final class AgentToolRequest {
    private final String conversationId;
    private final List<AgentChatMessage> messages;
    private final String latestUserMessage;

    public AgentToolRequest(String conversationId, List<AgentChatMessage> messages, String latestUserMessage) {
        this.conversationId = conversationId == null ? "" : conversationId;
        this.messages = List.copyOf(messages);
        this.latestUserMessage = latestUserMessage == null ? "" : latestUserMessage;
    }

    public String getConversationId() {
        return conversationId;
    }

    public List<AgentChatMessage> getMessages() {
        return messages;
    }

    public String getLatestUserMessage() {
        return latestUserMessage;
    }
}
