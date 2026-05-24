package com.wens.breeding.graph.llm;

public class LlmGatewayException extends RuntimeException {
    private final boolean retryable;

    public LlmGatewayException(String message, boolean retryable) {
        super(message);
        this.retryable = retryable;
    }

    public LlmGatewayException(String message, boolean retryable, Throwable cause) {
        super(message, cause);
        this.retryable = retryable;
    }

    public boolean isRetryable() {
        return retryable;
    }
}
