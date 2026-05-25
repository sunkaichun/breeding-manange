package com.wens.breeding.lark.bot.queue;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import com.wens.breeding.lark.bot.event.BotMessageEvent;
import com.wens.breeding.lark.bot.event.BotMessageEventHandler;

public final class QueuedBotMessageEventHandler implements BotMessageEventHandler, AutoCloseable {
    private final BotMessageEventHandler delegate;
    private final ScheduledExecutorService executor;
    private final Duration delay;
    private final boolean shutdownExecutorOnClose;
    private final ConcurrentMap<String, CompletableFuture<Void>> queueTails = new ConcurrentHashMap<>();

    public QueuedBotMessageEventHandler(
            BotMessageEventHandler delegate,
            ScheduledExecutorService executor,
            Duration delay) {
        this(delegate, executor, delay, false);
    }

    public QueuedBotMessageEventHandler(
            BotMessageEventHandler delegate,
            ScheduledExecutorService executor,
            Duration delay,
            boolean shutdownExecutorOnClose) {
        this.delegate = Objects.requireNonNull(delegate, "delegate");
        this.executor = Objects.requireNonNull(executor, "executor");
        this.delay = normalizeDelay(delay);
        this.shutdownExecutorOnClose = shutdownExecutorOnClose;
    }

    @Override
    public void onEvent(BotMessageEvent event) {
        submit(event);
    }

    public CompletableFuture<Void> submit(BotMessageEvent event) {
        Objects.requireNonNull(event, "event");
        String queueKey = event.getChatId();
        AtomicReference<CompletableFuture<Void>> submitted = new AtomicReference<>();
        queueTails.compute(queueKey, (key, currentTail) -> {
            CompletableFuture<Void> previous = currentTail == null
                    ? CompletableFuture.completedFuture(null)
                    : currentTail.handle((ignored, failure) -> null);
            CompletableFuture<Void> next = previous
                    .thenCompose(ignored -> delay())
                    .thenRun(() -> delegate.onEvent(event));
            next.whenComplete((ignored, failure) -> queueTails.remove(key, next));
            submitted.set(next);
            return next;
        });
        return submitted.get();
    }

    @Override
    public void close() {
        if (shutdownExecutorOnClose) {
            executor.shutdown();
        }
    }

    private CompletableFuture<Void> delay() {
        if (delay.isZero()) {
            return CompletableFuture.completedFuture(null);
        }
        CompletableFuture<Void> delayed = new CompletableFuture<>();
        executor.schedule(() -> delayed.complete(null), delay.toMillis(), TimeUnit.MILLISECONDS);
        return delayed;
    }

    private static Duration normalizeDelay(Duration delay) {
        if (delay == null || delay.isNegative()) {
            return Duration.ZERO;
        }
        return delay;
    }
}
