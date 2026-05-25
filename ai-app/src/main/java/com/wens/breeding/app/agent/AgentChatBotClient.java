package com.wens.breeding.app.agent;

import java.util.List;

import com.wens.breeding.lark.bot.chat.BotAgentChatClient;
import com.wens.breeding.lark.bot.chat.BotAgentChatEventSink;
import com.wens.breeding.lark.bot.chat.BotAgentChatMessage;
import com.wens.breeding.lark.bot.chat.BotAgentChatRequest;

public final class AgentChatBotClient implements BotAgentChatClient {
    private final AgentChatService agentChatService;

    public AgentChatBotClient(AgentChatService agentChatService) {
        this.agentChatService = java.util.Objects.requireNonNull(agentChatService, "agentChatService");
    }

    @Override
    public void stream(BotAgentChatRequest request, BotAgentChatEventSink eventSink) {
        agentChatService.stream(toAgentRequest(request), new BotAgentEventSink(eventSink));
    }

    private static AgentChatRequest toAgentRequest(BotAgentChatRequest request) {
        AgentChatRequest agentRequest = new AgentChatRequest();
        agentRequest.setConversationId(request.getConversationId());
        agentRequest.setEnableTools(request.isEnableTools());
        agentRequest.setMessages(toAgentMessages(request.getMessages()));
        return agentRequest;
    }

    private static List<AgentChatMessage> toAgentMessages(List<BotAgentChatMessage> messages) {
        return messages.stream()
                .map(AgentChatBotClient::toAgentMessage)
                .toList();
    }

    private static AgentChatMessage toAgentMessage(BotAgentChatMessage message) {
        AgentChatMessage agentMessage = new AgentChatMessage();
        agentMessage.setRole(message.getRole());
        agentMessage.setContent(message.getContent());
        return agentMessage;
    }

    private static final class BotAgentEventSink implements AgentEventSink {
        private final BotAgentChatEventSink delegate;

        private BotAgentEventSink(BotAgentChatEventSink delegate) {
            this.delegate = java.util.Objects.requireNonNull(delegate, "delegate");
        }

        @Override
        public void onToken(String token) {
            delegate.onToken(token);
        }

        @Override
        public void onToolCall(String toolName, java.util.Map<String, Object> arguments) {
            delegate.onToolCall(toolName);
        }

        @Override
        public void onToolResult(AgentToolResult result) {
            delegate.onToolResult(result.getToolName(), result.getSummary());
        }
    }
}
