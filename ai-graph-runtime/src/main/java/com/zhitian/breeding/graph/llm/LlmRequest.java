package com.zhitian.breeding.graph.llm;

import java.time.Duration;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public final class LlmRequest {
    private final String systemPrompt;
    private final String userPrompt;
    private final String responseSchema;
    private final double temperature;
    private final int maxTokens;
    private final Duration timeout;
    private final Map<String, String> metadata;

    private LlmRequest(Builder builder) {
        this.systemPrompt = requireText(builder.systemPrompt, "systemPrompt");
        this.userPrompt = requireText(builder.userPrompt, "userPrompt");
        this.responseSchema = builder.responseSchema == null ? "" : builder.responseSchema;
        this.temperature = builder.temperature;
        this.maxTokens = builder.maxTokens;
        this.timeout = Objects.requireNonNull(builder.timeout, "timeout");
        this.metadata = Collections.unmodifiableMap(new LinkedHashMap<>(builder.metadata));
        if (temperature < 0 || temperature > 2) {
            throw new IllegalArgumentException("temperature must be between 0 and 2");
        }
        if (maxTokens <= 0) {
            throw new IllegalArgumentException("maxTokens must be positive");
        }
        if (timeout.isZero() || timeout.isNegative()) {
            throw new IllegalArgumentException("timeout must be positive");
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getSystemPrompt() {
        return systemPrompt;
    }

    public String getUserPrompt() {
        return userPrompt;
    }

    public String getResponseSchema() {
        return responseSchema;
    }

    public double getTemperature() {
        return temperature;
    }

    public int getMaxTokens() {
        return maxTokens;
    }

    public Duration getTimeout() {
        return timeout;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value;
    }

    public static final class Builder {
        private String systemPrompt = "You are a precise breeding management analysis assistant.";
        private String userPrompt;
        private String responseSchema;
        private double temperature = 0.2;
        private int maxTokens = 1200;
        private Duration timeout = Duration.ofSeconds(30);
        private final Map<String, String> metadata = new LinkedHashMap<>();

        private Builder() {
        }

        public Builder systemPrompt(String systemPrompt) {
            this.systemPrompt = systemPrompt;
            return this;
        }

        public Builder userPrompt(String userPrompt) {
            this.userPrompt = userPrompt;
            return this;
        }

        public Builder responseSchema(String responseSchema) {
            this.responseSchema = responseSchema;
            return this;
        }

        public Builder temperature(double temperature) {
            this.temperature = temperature;
            return this;
        }

        public Builder maxTokens(int maxTokens) {
            this.maxTokens = maxTokens;
            return this;
        }

        public Builder timeout(Duration timeout) {
            this.timeout = timeout;
            return this;
        }

        public Builder metadata(String key, String value) {
            this.metadata.put(requireText(key, "metadata key"), requireText(value, "metadata value"));
            return this;
        }

        public LlmRequest build() {
            return new LlmRequest(this);
        }
    }
}
