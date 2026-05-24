package com.zhitian.breeding.graph.llm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

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
}
