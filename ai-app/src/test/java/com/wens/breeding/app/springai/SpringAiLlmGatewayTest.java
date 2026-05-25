package com.wens.breeding.app.springai;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;

import com.wens.breeding.graph.llm.LlmRequest;
import com.wens.breeding.graph.llm.LlmResponse;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.metadata.ChatGenerationMetadata;
import org.springframework.ai.chat.metadata.ChatResponseMetadata;
import org.springframework.ai.chat.metadata.DefaultUsage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;

class SpringAiLlmGatewayTest {
    @Test
    void mapsLlmGatewayRequestToSpringAiPrompt() {
        AtomicInteger calls = new AtomicInteger();
        ChatModel chatModel = new ChatModel() {
            @Override
            public ChatResponse call(Prompt prompt) {
                calls.incrementAndGet();
                assertEquals(2, prompt.getInstructions().size());
                assertEquals("system", ((SystemMessage) prompt.getInstructions().get(0)).getText());
                assertTrue(((UserMessage) prompt.getInstructions().get(1)).getText().contains("Return format"));
                assertEquals(0.3, prompt.getOptions().getTemperature());
                assertEquals(512, prompt.getOptions().getMaxTokens());
                return new ChatResponse(
                        Collections.singletonList(new Generation(
                                new AssistantMessage("{\"answer\":\"ok\"}"),
                                ChatGenerationMetadata.builder().finishReason("STOP").build())),
                        ChatResponseMetadata.builder()
                                .model("spring-test-model")
                                .usage(new DefaultUsage(13, 8))
                                .build());
            }
        };

        LlmResponse response = new SpringAiLlmGateway(chatModel, "fallback")
                .complete(LlmRequest.builder()
                        .systemPrompt("system")
                        .userPrompt("query")
                        .responseSchema("{\"answer\":\"string\"}")
                        .temperature(0.3)
                        .maxTokens(512)
                        .build());

        assertEquals(1, calls.get());
        assertEquals("{\"answer\":\"ok\"}", response.getContent());
        assertEquals("spring-test-model", response.getModelName());
        assertEquals(13, response.getPromptTokens());
        assertEquals(8, response.getCompletionTokens());
        assertEquals("STOP", response.getFinishReason());
    }
}
