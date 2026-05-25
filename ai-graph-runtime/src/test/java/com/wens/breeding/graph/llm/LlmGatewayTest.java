package com.wens.breeding.graph.llm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.output.FinishReason;
import dev.langchain4j.model.output.TokenUsage;

class LlmGatewayTest {
    @Test
    void staticGatewayReturnsStructuredJsonResponse() {
        LlmGateway gateway = new StaticJsonLlmGateway("{\"riskLevel\":\"LOW\",\"summary\":\"ok\"}");

        LlmResponse response = gateway.complete(LlmRequest.builder()
                .userPrompt("Summarize batch BATCH-001")
                .responseSchema("{\"riskLevel\":\"string\",\"summary\":\"string\"}")
                .timeout(Duration.ofSeconds(5))
                .metadata("requestId", "REQ-001")
                .build());

        assertEquals("static-json-test-model", response.getModelName());
        assertTrue(response.getContent().contains("\"riskLevel\""));
        assertTrue(response.getTotalTokens() > 0);
    }

    @Test
    void retryingGatewayRetriesRetryableFailures() {
        AtomicInteger attempts = new AtomicInteger();
        LlmGateway flakyGateway = request -> {
            if (attempts.incrementAndGet() == 1) {
                throw new LlmGatewayException("temporary timeout", true);
            }
            return new LlmResponse("{\"summary\":\"recovered\"}", "fake", 1, 1, "stop");
        };

        LlmResponse response = new RetryingLlmGateway(flakyGateway, 2)
                .complete(LlmRequest.builder().userPrompt("hello").build());

        assertEquals("{\"summary\":\"recovered\"}", response.getContent());
        assertEquals(2, attempts.get());
    }

    @Test
    void retryingGatewayDoesNotRetryNonRetryableFailures() {
        AtomicInteger attempts = new AtomicInteger();
        LlmGateway failingGateway = request -> {
            attempts.incrementAndGet();
            throw new LlmGatewayException("invalid api key", false);
        };

        assertThrows(LlmGatewayException.class, () -> new RetryingLlmGateway(failingGateway, 3)
                .complete(LlmRequest.builder().userPrompt("hello").build()));
        assertEquals(1, attempts.get());
    }

    @Test
    void requestRejectsInvalidTimeout() {
        assertThrows(IllegalArgumentException.class, () -> LlmRequest.builder()
                .userPrompt("hello")
                .timeout(Duration.ZERO)
                .build());
    }

    @Test
    void langChain4jGatewayMapsRequestAndResponse() {
        AtomicInteger calls = new AtomicInteger();
        ChatModel chatModel = new ChatModel() {
            @Override
            public ChatResponse chat(ChatRequest request) {
                calls.incrementAndGet();
                assertEquals(2, request.messages().size());
                assertEquals("system", ((SystemMessage) request.messages().get(0)).text());
                assertTrue(((UserMessage) request.messages().get(1)).singleText().contains("Return format"));
                assertEquals(0.4, request.temperature());
                assertEquals(256, request.maxOutputTokens());
                return ChatResponse.builder()
                        .aiMessage(AiMessage.from("{\"summary\":\"ok\"}"))
                        .modelName("lc-test-model")
                        .tokenUsage(new TokenUsage(11, 7))
                        .finishReason(FinishReason.STOP)
                        .build();
            }
        };

        LlmResponse response = new LangChain4jLlmGateway(chatModel, "fallback")
                .complete(LlmRequest.builder()
                        .systemPrompt("system")
                        .userPrompt("analyze")
                        .responseSchema("{\"summary\":\"string\"}")
                        .temperature(0.4)
                        .maxTokens(256)
                        .build());

        assertEquals(1, calls.get());
        assertEquals("{\"summary\":\"ok\"}", response.getContent());
        assertEquals("lc-test-model", response.getModelName());
        assertEquals(11, response.getPromptTokens());
        assertEquals(7, response.getCompletionTokens());
        assertEquals("STOP", response.getFinishReason());
    }
}
