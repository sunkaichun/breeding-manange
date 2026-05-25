package com.wens.breeding.graph.llm;

import java.util.Objects;

import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.output.TokenUsage;

public final class LangChain4jLlmGateway implements LlmGateway {
    private final ChatModel chatModel;
    private final String fallbackModelName;

    public LangChain4jLlmGateway(ChatModel chatModel, String fallbackModelName) {
        this.chatModel = Objects.requireNonNull(chatModel, "chatModel");
        this.fallbackModelName = fallbackModelName == null || fallbackModelName.trim().isEmpty()
                ? "langchain4j-chat-model"
                : fallbackModelName;
    }

    @Override
    public LlmResponse complete(LlmRequest request) {
        Objects.requireNonNull(request, "request");
        ChatRequest chatRequest = ChatRequest.builder()
                .messages(
                        SystemMessage.from(request.getSystemPrompt()),
                        UserMessage.from(userPrompt(request)))
                .temperature(request.getTemperature())
                .maxOutputTokens(request.getMaxTokens())
                .build();

        try {
            ChatResponse response = chatModel.chat(chatRequest);
            String content = response.aiMessage() == null ? "" : response.aiMessage().text();
            TokenUsage tokenUsage = response.tokenUsage();
            return new LlmResponse(
                    content,
                    modelName(response),
                    tokenUsage == null || tokenUsage.inputTokenCount() == null ? 0 : tokenUsage.inputTokenCount(),
                    tokenUsage == null || tokenUsage.outputTokenCount() == null ? 0 : tokenUsage.outputTokenCount(),
                    response.finishReason() == null ? "" : response.finishReason().name());
        } catch (RuntimeException exception) {
            throw new LlmGatewayException("LangChain4j model request failed: " + exception.getMessage(), true, exception);
        }
    }

    private String modelName(ChatResponse response) {
        String modelName = response.modelName();
        if (modelName == null || modelName.trim().isEmpty()) {
            modelName = response.metadata() == null ? "" : response.metadata().modelName();
        }
        return modelName == null || modelName.trim().isEmpty() ? fallbackModelName : modelName;
    }

    private static String userPrompt(LlmRequest request) {
        if (request.getResponseSchema().trim().isEmpty()) {
            return request.getUserPrompt();
        }
        return request.getUserPrompt() + "\n\nReturn format:\n" + request.getResponseSchema();
    }
}
