package com.wens.breeding.lark.bot.workflow;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import com.wens.breeding.lark.bot.chat.BotAgentChatClient;
import com.wens.breeding.lark.bot.chat.BotAgentChatEventSink;
import com.wens.breeding.lark.bot.chat.BotAgentChatRequest;
import com.wens.breeding.lark.bot.event.BotMessageEvent;
import com.wens.breeding.lark.im.InMemoryLarkImClient;
import com.wens.breeding.lark.im.LarkMessageType;

import org.junit.jupiter.api.Test;

class BotAgentChatWorkflowTest {
    @Test
    void sendsSingleFinalReplyFromAgentTokens() {
        CapturingChatClient chatClient = new CapturingChatClient();
        InMemoryLarkImClient imClient = new InMemoryLarkImClient();
        BotAgentChatWorkflow workflow = new BotAgentChatWorkflow(chatClient, imClient);

        BotAgentChatResult result = workflow.handle(event("om-001", "帮我分析 BATCH-001"));

        assertEquals(BotAgentChatStatus.COMPLETED, result.getStatus());
        assertEquals("oc_chat", chatClient.lastRequest.getConversationId());
        assertEquals("帮我分析 BATCH-001", chatClient.lastRequest.getMessages().get(0).getContent());
        assertEquals(1, imClient.listSentMessages().size());
        assertEquals(LarkMessageType.TEXT, imClient.listSentMessages().get(0).getMessageType());
        assertEquals("hello world", imClient.listSentMessages().get(0).getText());
    }

    @Test
    void skipsNonTextMessages() {
        InMemoryLarkImClient imClient = new InMemoryLarkImClient();
        BotAgentChatWorkflow workflow = new BotAgentChatWorkflow(new CapturingChatClient(), imClient);

        BotAgentChatResult result = workflow.handle(nonTextEvent());

        assertEquals(BotAgentChatStatus.SKIPPED, result.getStatus());
        assertTrue(imClient.listSentMessages().isEmpty());
    }

    @Test
    void sendsSingleErrorMessageWhenAgentFails() {
        InMemoryLarkImClient imClient = new InMemoryLarkImClient();
        BotAgentChatWorkflow workflow = new BotAgentChatWorkflow((request, sink) -> {
            throw new IllegalStateException("model unavailable");
        }, imClient);

        BotAgentChatResult result = workflow.handle(event("om-001", "你好"));

        assertEquals(BotAgentChatStatus.FAILED, result.getStatus());
        assertEquals(1, imClient.listSentMessages().size());
        assertEquals(LarkMessageType.ERROR, imClient.listSentMessages().get(0).getMessageType());
        assertTrue(imClient.listSentMessages().get(0).getText().contains("model unavailable"));
    }

    private static BotMessageEvent event(String messageId, String content) {
        return new BotMessageEvent(
                "evt-" + messageId,
                "im.message.receive_v1",
                "ou_sender",
                "oc_chat",
                "group",
                messageId,
                "text",
                content,
                "1780000000000",
                "");
    }

    private static BotMessageEvent nonTextEvent() {
        return new BotMessageEvent(
                "evt-card",
                "im.message.receive_v1",
                "ou_sender",
                "oc_chat",
                "group",
                "om-card",
                "interactive",
                "{}",
                "1780000000000",
                "");
    }

    private static final class CapturingChatClient implements BotAgentChatClient {
        private BotAgentChatRequest lastRequest;
        private final List<String> tokens = new ArrayList<>(List.of("hello ", "world"));

        @Override
        public void stream(BotAgentChatRequest request, BotAgentChatEventSink eventSink) {
            this.lastRequest = request;
            eventSink.onToolCall("test_tool");
            eventSink.onToolResult("test_tool", "ok");
            for (String token : tokens) {
                eventSink.onToken(token);
            }
        }
    }
}
