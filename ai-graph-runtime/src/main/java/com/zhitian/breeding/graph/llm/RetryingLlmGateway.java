package com.zhitian.breeding.graph.llm;

import java.util.Objects;

public final class RetryingLlmGateway implements LlmGateway {
    private final LlmGateway delegate;
    private final int maxAttempts;

    public RetryingLlmGateway(LlmGateway delegate, int maxAttempts) {
        this.delegate = Objects.requireNonNull(delegate, "delegate");
        if (maxAttempts <= 0) {
            throw new IllegalArgumentException("maxAttempts must be positive");
        }
        this.maxAttempts = maxAttempts;
    }

    @Override
    public LlmResponse complete(LlmRequest request) {
        LlmGatewayException lastFailure = null;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return delegate.complete(request);
            } catch (LlmGatewayException ex) {
                lastFailure = ex;
                if (!ex.isRetryable() || attempt == maxAttempts) {
                    throw ex;
                }
            }
        }
        throw lastFailure == null
                ? new LlmGatewayException("LLM gateway failed without a captured cause", false)
                : lastFailure;
    }
}
