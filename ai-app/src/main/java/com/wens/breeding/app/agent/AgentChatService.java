package com.wens.breeding.app.agent;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class AgentChatService {
    private static final String SYSTEM_PROMPT = "You are a general-purpose AI agent for the breeding management backend. "
            + "You can answer normal questions directly. When tool results are provided, use them as trusted context. "
            + "Do not claim that you called tools that are not present in the tool context.";

    private final AgentToolRouter toolRouter;
    private final AgentChatClient chatClient;

    public AgentChatService(AgentToolRouter toolRouter, AgentChatClient chatClient) {
        this.toolRouter = toolRouter;
        this.chatClient = chatClient;
    }

    public void stream(AgentChatRequest request, AgentEventSink eventSink) {
        ValidatedChat validatedChat = validate(request);
        List<AgentToolResult> toolResults = new ArrayList<>();
        if (request.isEnableTools()) {
            AgentToolRequest toolRequest = new AgentToolRequest(
                    request.getConversationId(),
                    validatedChat.messages,
                    validatedChat.latestUserMessage);
            for (AgentTool tool : toolRouter.route(toolRequest)) {
                eventSink.onToolCall(tool.name(), toolArguments(tool, toolRequest));
                AgentToolResult result = tool.execute(toolRequest);
                toolResults.add(result);
                eventSink.onToolResult(result);
            }
        }

        chatClient.stream(buildPrompt(validatedChat.messages, toolResults), eventSink::onToken);
    }

    private static AgentChatPrompt buildPrompt(List<AgentChatMessage> messages, List<AgentToolResult> toolResults) {
        StringBuilder userPrompt = new StringBuilder();
        userPrompt.append("Conversation:\n");
        for (AgentChatMessage message : messages) {
            userPrompt.append(message.getRole()).append(": ").append(message.getContent()).append('\n');
        }
        if (!toolResults.isEmpty()) {
            userPrompt.append("\nTool context:\n");
            for (AgentToolResult result : toolResults) {
                userPrompt.append(result.toContextBlock()).append("\n\n");
            }
        }
        userPrompt.append("Please answer the latest user message.");
        return new AgentChatPrompt(SYSTEM_PROMPT, userPrompt.toString());
    }

    private static java.util.Map<String, Object> toolArguments(AgentTool tool, AgentToolRequest request) {
        java.util.Map<String, Object> arguments = new java.util.LinkedHashMap<>();
        BatchIdExtractor.extract(request.getLatestUserMessage()).ifPresent(batchId -> arguments.put("batchId", batchId));
        arguments.put("matchedBy", tool.name());
        return arguments;
    }

    private static ValidatedChat validate(AgentChatRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("request must not be null");
        }
        List<AgentChatMessage> messages = request.getMessages();
        if (messages.isEmpty()) {
            throw new IllegalArgumentException("messages must not be empty");
        }
        List<AgentChatMessage> normalized = new ArrayList<>();
        String latestUserMessage = "";
        for (AgentChatMessage message : messages) {
            String role = requireText(message.getRole(), "message.role").trim().toLowerCase(java.util.Locale.ROOT);
            if (!role.equals("system") && !role.equals("user") && !role.equals("assistant")) {
                throw new IllegalArgumentException("message.role is not supported: " + message.getRole());
            }
            String content = requireText(message.getContent(), "message.content");
            AgentChatMessage normalizedMessage = new AgentChatMessage();
            normalizedMessage.setRole(role);
            normalizedMessage.setContent(content);
            normalized.add(normalizedMessage);
            if (role.equals("user")) {
                latestUserMessage = content;
            }
        }
        if (latestUserMessage.trim().isEmpty()) {
            throw new IllegalArgumentException("messages must include at least one user message");
        }
        return new ValidatedChat(normalized, latestUserMessage);
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value;
    }

    private static final class ValidatedChat {
        private final List<AgentChatMessage> messages;
        private final String latestUserMessage;

        private ValidatedChat(List<AgentChatMessage> messages, String latestUserMessage) {
            this.messages = messages;
            this.latestUserMessage = latestUserMessage;
        }
    }
}
