package com.wens.breeding.lark.bot.runner;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.wens.breeding.lark.bot.event.ImMessageReceiveV1Schema;

public final class LarkEventConsumerConfig {
    private final String cliPath;
    private final String eventKey;
    private final LarkEventConsumerIdentity identity;
    private final int maxEvents;
    private final String timeout;
    private final String jqExpression;
    private final Duration readyTimeout;

    private LarkEventConsumerConfig(Builder builder) {
        this.cliPath = requireText(builder.cliPath, "cliPath");
        this.eventKey = requireText(builder.eventKey, "eventKey");
        this.identity = builder.identity == null ? LarkEventConsumerIdentity.BOT : builder.identity;
        this.maxEvents = builder.maxEvents;
        this.timeout = builder.timeout == null ? "" : builder.timeout;
        this.jqExpression = builder.jqExpression == null ? "" : builder.jqExpression;
        this.readyTimeout = builder.readyTimeout == null ? Duration.ofSeconds(10) : builder.readyTimeout;
        if (maxEvents < 0) {
            throw new IllegalArgumentException("maxEvents must be non-negative");
        }
        if (readyTimeout.isNegative() || readyTimeout.isZero()) {
            throw new IllegalArgumentException("readyTimeout must be positive");
        }
    }

    public static Builder receiveMessageDefaults() {
        return new Builder().eventKey(ImMessageReceiveV1Schema.EVENT_KEY).identity(LarkEventConsumerIdentity.BOT);
    }

    public List<String> toCommand() {
        List<String> command = new ArrayList<>();
        command.add(cliPath);
        command.add("event");
        command.add("consume");
        command.add(eventKey);
        command.add("--as");
        command.add(identity.getCliValue());
        if (maxEvents > 0) {
            command.add("--max-events");
            command.add(Integer.toString(maxEvents));
        }
        if (!timeout.isEmpty()) {
            command.add("--timeout");
            command.add(timeout);
        }
        if (!jqExpression.isEmpty()) {
            command.add("--jq");
            command.add(jqExpression);
        }
        return Collections.unmodifiableList(command);
    }

    public String getCliPath() {
        return cliPath;
    }

    public String getEventKey() {
        return eventKey;
    }

    public LarkEventConsumerIdentity getIdentity() {
        return identity;
    }

    public int getMaxEvents() {
        return maxEvents;
    }

    public String getTimeout() {
        return timeout;
    }

    public String getJqExpression() {
        return jqExpression;
    }

    public Duration getReadyTimeout() {
        return readyTimeout;
    }

    public static final class Builder {
        private String cliPath = "lark-cli";
        private String eventKey;
        private LarkEventConsumerIdentity identity = LarkEventConsumerIdentity.BOT;
        private int maxEvents;
        private String timeout;
        private String jqExpression;
        private Duration readyTimeout = Duration.ofSeconds(10);

        public Builder cliPath(String cliPath) {
            this.cliPath = cliPath;
            return this;
        }

        public Builder eventKey(String eventKey) {
            this.eventKey = eventKey;
            return this;
        }

        public Builder identity(LarkEventConsumerIdentity identity) {
            this.identity = identity;
            return this;
        }

        public Builder maxEvents(int maxEvents) {
            this.maxEvents = maxEvents;
            return this;
        }

        public Builder timeout(String timeout) {
            this.timeout = timeout;
            return this;
        }

        public Builder jqExpression(String jqExpression) {
            this.jqExpression = jqExpression;
            return this;
        }

        public Builder readyTimeout(Duration readyTimeout) {
            this.readyTimeout = readyTimeout;
            return this;
        }

        public LarkEventConsumerConfig build() {
            return new LarkEventConsumerConfig(this);
        }
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value;
    }
}
