package com.wens.breeding.lark.im;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class InMemoryLarkImClientTest {
    private final InMemoryLarkImClient client = new InMemoryLarkImClient();

    @Test
    void sendsTextMessageToChat() {
        MessageDeliveryResult result = client.sendText("oc_test", "Analysis task started");

        assertTrue(result.isDelivered());
        assertEquals("im-msg-1", result.getMessageId());
        assertEquals("oc_test", result.getChatId());
        assertEquals(LarkMessageType.TEXT, result.getMessageType());
        assertEquals("Analysis task started", client.listSentMessages().get(0).getText());
    }

    @Test
    void sendsCardMessageToChat() {
        LarkCardMessage card = LarkCardMessage
                .builder("Weight trend analysis completed", "Current risk level: HIGH")
                .field("batch", "BATCH-001")
                .field("dateRange", "2026-05-20 to 2026-05-22")
                .action("View Base", "https://example.feishu.cn/base/test")
                .build();

        MessageDeliveryResult result = client.sendCard("oc_test", card);

        assertEquals(LarkMessageType.CARD, result.getMessageType());
        assertEquals("Weight trend analysis completed", client.listSentMessages().get(0).getCardMessage().getTitle());
        assertEquals("BATCH-001", client.listSentMessages().get(0).getCardMessage().getFields().get("batch"));
        assertEquals(1, client.listSentMessages().get(0).getCardMessage().getActions().size());
    }

    @Test
    void sendsErrorMessageAsErrorCard() {
        MessageDeliveryResult result = client.sendError("oc_test", "Analysis failed", "Batch BATCH-404 was not found");

        assertEquals(LarkMessageType.ERROR, result.getMessageType());
        SentLarkMessage message = client.listSentMessages().get(0);
        assertEquals("Batch BATCH-404 was not found", message.getText());
        assertEquals("ERROR", message.getCardMessage().getFields().get("status"));
    }

    @Test
    void rejectsBlankChatId() {
        assertThrows(IllegalArgumentException.class, () -> client.sendText(" ", "hello"));
    }
}
