package com.wens.breeding.app.config;

import java.time.Duration;

public final class LarkBotProperties {
    private Duration queueDelay = Duration.ofMillis(500);
    private int queueThreads = 2;

    public Duration getQueueDelay() {
        return queueDelay;
    }

    public void setQueueDelay(Duration queueDelay) {
        this.queueDelay = queueDelay == null ? Duration.ZERO : queueDelay;
    }

    public int getQueueThreads() {
        return queueThreads;
    }

    public void setQueueThreads(int queueThreads) {
        this.queueThreads = queueThreads;
    }

    public int resolvedQueueThreads() {
        return Math.max(1, queueThreads);
    }
}
