package com.wens.breeding.lark.bot.dedupe;

import java.time.Instant;

public final class MessageProcessingRecord {
    private final String key;
    private final MessageProcessingStatus status;
    private final Instant updatedAt;
    private final String failureReason;

    public MessageProcessingRecord(
            String key,
            MessageProcessingStatus status,
            Instant updatedAt,
            String failureReason) {
        this.key = Texts.requireText(key, "key");
        this.status = status == null ? MessageProcessingStatus.PROCESSING : status;
        this.updatedAt = updatedAt == null ? Instant.EPOCH : updatedAt;
        this.failureReason = failureReason == null ? "" : failureReason;
    }

    public String getKey() {
        return key;
    }

    public MessageProcessingStatus getStatus() {
        return status;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public String getFailureReason() {
        return failureReason;
    }
}
