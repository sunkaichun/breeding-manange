package com.wens.breeding.app.agent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class AgentChatRequest {
    private String conversationId;
    private List<AgentChatMessage> messages = new ArrayList<>();
    private Boolean enableTools = Boolean.TRUE;

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public List<AgentChatMessage> getMessages() {
        return messages == null ? Collections.emptyList() : Collections.unmodifiableList(messages);
    }

    public void setMessages(List<AgentChatMessage> messages) {
        this.messages = messages == null ? new ArrayList<>() : new ArrayList<>(messages);
    }

    public boolean isEnableTools() {
        return enableTools == null || enableTools;
    }

    public void setEnableTools(Boolean enableTools) {
        this.enableTools = enableTools;
    }
}
