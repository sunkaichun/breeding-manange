package com.wens.breeding.lark.bot.dedupe;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.wens.breeding.lark.bot.event.BotMessageEvent;

class IdempotentBotMessageEventHandlerTest {
    @Test
    void processesSameEventOnlyOnce() {
        InMemoryMessageDeduplicationStore store = new InMemoryMessageDeduplicationStore();
        List<BotMessageEvent> handledEvents = new ArrayList<>();
        IdempotentBotMessageEventHandler handler = new IdempotentBotMessageEventHandler(store, handledEvents::add);
        BotMessageEvent event = event("evt-001", "om-001");

        handler.onEvent(event);
        handler.onEvent(event);

        assertEquals(1, handledEvents.size());
        assertEquals(MessageProcessingStatus.PROCESSED, store.findRecord("event:evt-001").get().getStatus());
    }

    @Test
    void skipsEventThatIsAlreadyProcessing() {
        InMemoryMessageDeduplicationStore store = new InMemoryMessageDeduplicationStore();
        assertTrue(store.tryStartProcessing("event:evt-001"));
        List<BotMessageEvent> handledEvents = new ArrayList<>();
        IdempotentBotMessageEventHandler handler = new IdempotentBotMessageEventHandler(store, handledEvents::add);

        handler.onEvent(event("evt-001", "om-001"));

        assertTrue(handledEvents.isEmpty());
        assertEquals(MessageProcessingStatus.PROCESSING, store.findRecord("event:evt-001").get().getStatus());
    }

    @Test
    void marksFailedWhenDelegateFailsAndAllowsRetry() {
        InMemoryMessageDeduplicationStore store = new InMemoryMessageDeduplicationStore();
        BotMessageEvent event = event("evt-001", "om-001");
        IdempotentBotMessageEventHandler failingHandler = new IdempotentBotMessageEventHandler(store, received -> {
            throw new IllegalStateException("handler failed");
        });

        assertThrows(IllegalStateException.class, () -> failingHandler.onEvent(event));
        assertEquals(MessageProcessingStatus.FAILED, store.findRecord("event:evt-001").get().getStatus());

        List<BotMessageEvent> handledEvents = new ArrayList<>();
        IdempotentBotMessageEventHandler retryHandler = new IdempotentBotMessageEventHandler(store, handledEvents::add);
        retryHandler.onEvent(event);

        assertEquals(1, handledEvents.size());
        assertEquals(MessageProcessingStatus.PROCESSED, store.findRecord("event:evt-001").get().getStatus());
    }

    @Test
    void storeRejectsDuplicateProcessedKey() {
        InMemoryMessageDeduplicationStore store = new InMemoryMessageDeduplicationStore();

        assertTrue(store.tryStartProcessing("event:evt-001"));
        store.markProcessed("event:evt-001");

        assertFalse(store.tryStartProcessing("event:evt-001"));
        assertEquals(MessageProcessingStatus.PROCESSED, store.findRecord("event:evt-001").get().getStatus());
    }

    private static BotMessageEvent event(String eventId, String messageId) {
        return new BotMessageEvent(
                eventId,
                "im.message.receive_v1",
                "ou_sender",
                "oc_chat",
                "group",
                messageId,
                "text",
                "Analyze BATCH-001 recent weight",
                "1779616799000",
                "1779616800000");
    }
}
