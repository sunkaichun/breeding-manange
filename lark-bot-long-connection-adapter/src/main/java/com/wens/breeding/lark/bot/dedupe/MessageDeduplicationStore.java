package com.wens.breeding.lark.bot.dedupe;

import java.util.Optional;

public interface MessageDeduplicationStore {
    boolean tryStartProcessing(String key);

    void markProcessed(String key);

    void markFailed(String key, String failureReason);

    Optional<MessageProcessingRecord> findRecord(String key);
}
