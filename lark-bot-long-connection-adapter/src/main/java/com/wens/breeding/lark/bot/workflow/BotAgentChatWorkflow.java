package com.wens.breeding.lark.bot.workflow;

import java.util.Objects;

import com.wens.breeding.lark.bot.chat.BotAgentChatClient;
import com.wens.breeding.lark.bot.chat.BotAgentChatEventSink;
import com.wens.breeding.lark.bot.chat.BotAgentChatRequest;
import com.wens.breeding.lark.bot.event.BotMessageEvent;
import com.wens.breeding.lark.bot.event.BotMessageEventHandler;
import com.wens.breeding.lark.im.LarkImClient;

public final class BotAgentChatWorkflow implements BotMessageEventHandler {
    private static final String EMPTY_REPLY_FALLBACK = "我已处理你的消息，但暂时没有生成可发送的回复。";

    private final BotAgentChatClient chatClient;
    private final LarkImClient imClient;
    private BotAgentChatResult lastResult;

    public BotAgentChatWorkflow(BotAgentChatClient chatClient, LarkImClient imClient) {
        this.chatClient = Objects.requireNonNull(chatClient, "chatClient");
        this.imClient = Objects.requireNonNull(imClient, "imClient");
    }

    @Override
    public void onEvent(BotMessageEvent event) {
        lastResult = handle(event);
    }

    public BotAgentChatResult handle(BotMessageEvent event) {
        Objects.requireNonNull(event, "event");
        if (!event.isTextMessage()) {
            return BotAgentChatResult.skipped(event.getChatId(), event.getMessageId(), "Only text messages are supported");
        }

        BotAgentChatRequest request = BotAgentChatRequest.singleUserMessage(event.getChatId(), event.getContent());
        CollectingChatSink sink = new CollectingChatSink();
        try {
            chatClient.stream(request, sink);
            String replyText = sink.replyText();
            imClient.sendText(event.getChatId(), replyText);
            return BotAgentChatResult.completed(event.getChatId(), event.getMessageId(), replyText);
        } catch (RuntimeException exception) {
            String errorMessage = exception.getMessage() == null ? "Agent chat failed" : exception.getMessage();
            imClient.sendError(event.getChatId(), "Agent chat failed", errorMessage);
            return BotAgentChatResult.failed(event.getChatId(), event.getMessageId(), errorMessage);
        }
    }

    public BotAgentChatResult getLastResult() {
        return lastResult;
    }

    private static final class CollectingChatSink implements BotAgentChatEventSink {
        private final StringBuilder tokens = new StringBuilder();

        @Override
        public void onToken(String token) {
            if (token != null && !token.isEmpty()) {
                tokens.append(token);
            }
        }

        @Override
        public void onToolCall(String toolName) {
            // The bot sends a single final reply, so intermediate tool events are intentionally not sent to IM.
        }

        @Override
        public void onToolResult(String toolName, String summary) {
            // Tool results are injected into the Agent prompt; the final model answer is the user-facing reply.
        }

        private String replyText() {
            String reply = tokens.toString().trim();
            return reply.isEmpty() ? EMPTY_REPLY_FALLBACK : reply;
        }
    }
}
