package com.wens.breeding.app.springai;

import java.util.Arrays;
import java.util.Objects;

import com.wens.breeding.graph.llm.LlmGateway;
import com.wens.breeding.graph.llm.LlmGatewayException;
import com.wens.breeding.graph.llm.LlmRequest;
import com.wens.breeding.graph.llm.LlmResponse;

import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.metadata.ChatResponseMetadata;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;

public final class SpringAiLlmGateway implements LlmGateway {
    private final ChatModel chatModel;
    private final String fallbackModelName;

    public SpringAiLlmGateway(ChatModel chatModel, String fallbackModelName) {
        this.chatModel = Objects.requireNonNull(chatModel, "chatModel");
        this.fallbackModelName = fallbackModelName == null || fallbackModelName.trim().isEmpty()
                ? "spring-ai-chat-model"
                : fallbackModelName;
    }

    @Override
    public LlmResponse complete(LlmRequest request) {
        Objects.requireNonNull(request, "request");
        Prompt prompt = new Prompt(
                Arrays.asList(
                        new SystemMessage(request.getSystemPrompt()),
                        new UserMessage(userPrompt(request))),
                ChatOptions.builder()
                        .temperature(request.getTemperature())
                        .maxTokens(request.getMaxTokens())
                        .build());

        try {
            ChatResponse response = chatModel.call(prompt);
            Generation generation = response.getResult();
            String content = generation == null || generation.getOutput() == null
                    ? ""
                    : generation.getOutput().getText();
            Usage usage = response.getMetadata() == null ? null : response.getMetadata().getUsage();
            return new LlmResponse(
                    content,
                    modelName(response.getMetadata()),
                    tokenCount(usage == null ? null : usage.getPromptTokens()),
                    tokenCount(usage == null ? null : usage.getCompletionTokens()),
                    generation == null || generation.getMetadata() == null
                            ? ""
                            : generation.getMetadata().getFinishReason());
        } catch (RuntimeException exception) {
            throw new LlmGatewayException("Spring AI model request failed: " + exception.getMessage(), true, exception);
        }
    }

    private String modelName(ChatResponseMetadata metadata) {
        String modelName = metadata == null ? "" : metadata.getModel();
        return modelName == null || modelName.trim().isEmpty() ? fallbackModelName : modelName;
    }

    private static int tokenCount(Integer value) {
        return value == null ? 0 : value;
    }

    private static String userPrompt(LlmRequest request) {
        if (request.getResponseSchema().trim().isEmpty()) {
            return request.getUserPrompt();
        }
        return request.getUserPrompt() + "\n\nReturn format:\n" + request.getResponseSchema();
    }
}
