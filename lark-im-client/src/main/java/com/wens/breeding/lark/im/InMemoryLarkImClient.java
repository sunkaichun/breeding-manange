package com.wens.breeding.lark.im;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public final class InMemoryLarkImClient implements LarkImClient {
    private final AtomicLong sequence = new AtomicLong();
    private final List<SentLarkMessage> sentMessages = new ArrayList<>();

    @Override
    public MessageDeliveryResult sendText(String chatId, String text) {
        String normalizedChatId = Texts.requireText(chatId, "chatId");
        String normalizedText = Texts.requireText(text, "text");
        String messageId = nextMessageId();
        append(new SentLarkMessage(messageId, normalizedChatId, LarkMessageType.TEXT, normalizedText, null));
        return delivered(messageId, normalizedChatId, LarkMessageType.TEXT);
    }

    @Override
    public MessageDeliveryResult sendCard(String chatId, LarkCardMessage cardMessage) {
        String normalizedChatId = Texts.requireText(chatId, "chatId");
        LarkCardMessage nonNullCard = java.util.Objects.requireNonNull(cardMessage, "cardMessage");
        String messageId = nextMessageId();
        append(new SentLarkMessage(messageId, normalizedChatId, LarkMessageType.CARD, nonNullCard.getSummary(), nonNullCard));
        return delivered(messageId, normalizedChatId, LarkMessageType.CARD);
    }

    @Override
    public MessageDeliveryResult sendError(String chatId, String title, String detail) {
        String normalizedChatId = Texts.requireText(chatId, "chatId");
        String normalizedTitle = Texts.requireText(title, "title");
        String normalizedDetail = Texts.requireText(detail, "detail");
        LarkCardMessage cardMessage = LarkCardMessage
                .builder(normalizedTitle, normalizedDetail)
                .field("status", "ERROR")
                .build();
        String messageId = nextMessageId();
        append(new SentLarkMessage(messageId, normalizedChatId, LarkMessageType.ERROR, normalizedDetail, cardMessage));
        return delivered(messageId, normalizedChatId, LarkMessageType.ERROR);
    }

    public List<SentLarkMessage> listSentMessages() {
        return Collections.unmodifiableList(new ArrayList<>(sentMessages));
    }

    private void append(SentLarkMessage message) {
        sentMessages.add(message);
    }

    private String nextMessageId() {
        return "im-msg-" + sequence.incrementAndGet();
    }

    private static MessageDeliveryResult delivered(String messageId, String chatId, LarkMessageType messageType) {
        return new MessageDeliveryResult(messageId, chatId, messageType, true);
    }
}
