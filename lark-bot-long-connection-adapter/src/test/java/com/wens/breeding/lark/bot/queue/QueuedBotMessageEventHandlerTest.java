package com.wens.breeding.lark.bot.queue;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import com.wens.breeding.lark.bot.event.BotMessageEvent;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class QueuedBotMessageEventHandlerTest {
    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(2);

    @AfterEach
    void tearDown() {
        executor.shutdownNow();
    }

    @Test
    void serializesMessagesFromSameChat() throws Exception {
        CountDownLatch firstStarted = new CountDownLatch(1);
        CountDownLatch releaseFirst = new CountDownLatch(1);
        AtomicInteger concurrent = new AtomicInteger();
        AtomicInteger maxConcurrent = new AtomicInteger();
        List<String> handled = java.util.Collections.synchronizedList(new ArrayList<>());

        QueuedBotMessageEventHandler handler = new QueuedBotMessageEventHandler(event -> {
            int running = concurrent.incrementAndGet();
            maxConcurrent.updateAndGet(current -> Math.max(current, running));
            handled.add(event.getMessageId());
            if ("om-001".equals(event.getMessageId())) {
                firstStarted.countDown();
                await(releaseFirst);
            }
            concurrent.decrementAndGet();
        }, executor, Duration.ofMillis(10));

        CompletableFuture<Void> first = handler.submit(event("oc_chat", "om-001"));
        CompletableFuture<Void> second = handler.submit(event("oc_chat", "om-002"));

        assertEquals(true, firstStarted.await(1, TimeUnit.SECONDS));
        assertFalse(second.isDone());
        releaseFirst.countDown();
        CompletableFuture.allOf(first, second).get(1, TimeUnit.SECONDS);

        assertEquals(List.of("om-001", "om-002"), handled);
        assertEquals(1, maxConcurrent.get());
    }

    @Test
    void allowsDifferentChatsToRunIndependently() throws Exception {
        CountDownLatch bothStarted = new CountDownLatch(2);
        CountDownLatch releaseBoth = new CountDownLatch(1);
        AtomicBoolean overlapped = new AtomicBoolean(false);
        AtomicInteger running = new AtomicInteger();

        QueuedBotMessageEventHandler handler = new QueuedBotMessageEventHandler(event -> {
            if (running.incrementAndGet() > 1) {
                overlapped.set(true);
            }
            bothStarted.countDown();
            await(releaseBoth);
            running.decrementAndGet();
        }, executor, Duration.ofMillis(10));

        CompletableFuture<Void> first = handler.submit(event("oc_a", "om-001"));
        CompletableFuture<Void> second = handler.submit(event("oc_b", "om-002"));

        assertEquals(true, bothStarted.await(1, TimeUnit.SECONDS));
        releaseBoth.countDown();
        CompletableFuture.allOf(first, second).get(1, TimeUnit.SECONDS);

        assertEquals(true, overlapped.get());
    }

    private static void await(CountDownLatch latch) {
        try {
            latch.await(1, TimeUnit.SECONDS);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(exception);
        }
    }

    private static BotMessageEvent event(String chatId, String messageId) {
        return new BotMessageEvent(
                "evt-" + messageId,
                "im.message.receive_v1",
                "ou_sender",
                chatId,
                "group",
                messageId,
                "text",
                "hello",
                "1780000000000",
                "");
    }
}
