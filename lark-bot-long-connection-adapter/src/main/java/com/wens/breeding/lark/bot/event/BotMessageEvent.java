package com.wens.breeding.lark.bot.event;

public final class BotMessageEvent {
    private final String eventId;
    private final String eventType;
    private final String senderOpenId;
    private final String chatId;
    private final String chatType;
    private final String messageId;
    private final String messageType;
    private final String content;
    private final String createTimeMillis;
    private final String deliveryTimeMillis;

    public BotMessageEvent(
            String eventId,
            String eventType,
            String senderOpenId,
            String chatId,
            String chatType,
            String messageId,
            String messageType,
            String content,
            String createTimeMillis,
            String deliveryTimeMillis) {
        this.eventId = Texts.requireText(eventId, "eventId");
        this.eventType = Texts.requireText(eventType, "eventType");
        this.senderOpenId = Texts.requireText(senderOpenId, "senderOpenId");
        this.chatId = Texts.requireText(chatId, "chatId");
        this.chatType = Texts.requireText(chatType, "chatType");
        this.messageId = Texts.requireText(messageId, "messageId");
        this.messageType = Texts.requireText(messageType, "messageType");
        this.content = Texts.requireText(content, "content");
        this.createTimeMillis = Texts.requireText(createTimeMillis, "createTimeMillis");
        this.deliveryTimeMillis = deliveryTimeMillis == null ? "" : deliveryTimeMillis;
    }

    public String getEventId() {
        return eventId;
    }

    public String getEventType() {
        return eventType;
    }

    public String getSenderOpenId() {
        return senderOpenId;
    }

    public String getChatId() {
        return chatId;
    }

    public String getChatType() {
        return chatType;
    }

    public String getMessageId() {
        return messageId;
    }

    public String getMessageType() {
        return messageType;
    }

    public String getContent() {
        return content;
    }

    public String getCreateTimeMillis() {
        return createTimeMillis;
    }

    public String getDeliveryTimeMillis() {
        return deliveryTimeMillis;
    }

    public boolean isTextMessage() {
        return "text".equals(messageType);
    }

    public boolean isInteractiveMessage() {
        return "interactive".equals(messageType);
    }
}
