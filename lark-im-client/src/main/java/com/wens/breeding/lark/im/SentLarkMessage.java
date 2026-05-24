package com.wens.breeding.lark.im;

public final class SentLarkMessage {
    private final String messageId;
    private final String chatId;
    private final LarkMessageType messageType;
    private final String text;
    private final LarkCardMessage cardMessage;

    public SentLarkMessage(
            String messageId,
            String chatId,
            LarkMessageType messageType,
            String text,
            LarkCardMessage cardMessage) {
        this.messageId = Texts.requireText(messageId, "messageId");
        this.chatId = Texts.requireText(chatId, "chatId");
        this.messageType = messageType == null ? LarkMessageType.TEXT : messageType;
        this.text = text == null ? "" : text;
        this.cardMessage = cardMessage;
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

    public String getText() {
        return text;
    }

    public LarkCardMessage getCardMessage() {
        return cardMessage;
    }
}
