package com.wens.breeding.lark.bot.dedupe;

import java.time.Clock;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class InMemoryMessageDeduplicationStore implements MessageDeduplicationStore {
    private final Clock clock;
    private final Map<String, MessageProcessingRecord> records = new LinkedHashMap<>();

    public InMemoryMessageDeduplicationStore() {
        this(Clock.systemUTC());
    }

    public InMemoryMessageDeduplicationStore(Clock clock) {
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    @Override
    public synchronized boolean tryStartProcessing(String key) {
        String normalizedKey = Texts.requireText(key, "key");
        MessageProcessingRecord existing = records.get(normalizedKey);
        if (existing != null && existing.getStatus() != MessageProcessingStatus.FAILED) {
            return false;
        }
        records.put(normalizedKey, new MessageProcessingRecord(
                normalizedKey,
                MessageProcessingStatus.PROCESSING,
                now(),
                ""));
        return true;
    }

    @Override
    public synchronized void markProcessed(String key) {
        String normalizedKey = Texts.requireText(key, "key");
        records.put(normalizedKey, new MessageProcessingRecord(
                normalizedKey,
                MessageProcessingStatus.PROCESSED,
                now(),
                ""));
    }

    @Override
    public synchronized void markFailed(String key, String failureReason) {
        String normalizedKey = Texts.requireText(key, "key");
        records.put(normalizedKey, new MessageProcessingRecord(
                normalizedKey,
                MessageProcessingStatus.FAILED,
                now(),
                failureReason));
    }

    @Override
    public synchronized Optional<MessageProcessingRecord> findRecord(String key) {
        return Optional.ofNullable(records.get(Texts.requireText(key, "key")));
    }

    private Instant now() {
        return Instant.now(clock);
    }
}
