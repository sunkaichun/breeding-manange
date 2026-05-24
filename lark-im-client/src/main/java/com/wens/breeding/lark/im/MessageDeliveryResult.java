package com.wens.breeding.lark.im;

public final class MessageDeliveryResult {
    private final String messageId;
    private final String chatId;
    private final LarkMessageType messageType;
    private final boolean delivered;

    public MessageDeliveryResult(
            String messageId,
            String chatId,
            LarkMessageType messageType,
            boolean delivered) {
        this.messageId = Texts.requireText(messageId, "messageId");
        this.chatId = Texts.requireText(chatId, "chatId");
        this.messageType = messageType == null ? LarkMessageType.TEXT : messageType;
        this.delivered = delivered;
    }

    public String getMessageId() {
        return messageId;
    }

    public String getChatId() {
        return chatId;
    }

    public LarkMessageType getMessageType() {
        return messageType;
    }

    public boolean isDelivered() {
        return delivered;
    }
}
