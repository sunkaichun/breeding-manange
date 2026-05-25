package com.wens.breeding.lark.bot.workflow;

public final class BotAgentChatResult {
    private final BotAgentChatStatus status;
    private final String conversationId;
    private final String messageId;
    private final String replyText;
    private final String errorMessage;

    private BotAgentChatResult(
            BotAgentChatStatus status,
            String conversationId,
            String messageId,
            String replyText,
            String errorMessage) {
        this.status = status;
        this.conversationId = conversationId == null ? "" : conversationId;
        this.messageId = messageId == null ? "" : messageId;
        this.replyText = replyText == null ? "" : replyText;
        this.errorMessage = errorMessage == null ? "" : errorMessage;
    }

    public static BotAgentChatResult completed(String conversationId, String messageId, String replyText) {
        return new BotAgentChatResult(BotAgentChatStatus.COMPLETED, conversationId, messageId, replyText, "");
    }

    public static BotAgentChatResult skipped(String conversationId, String messageId, String reason) {
        return new BotAgentChatResult(BotAgentChatStatus.SKIPPED, conversationId, messageId, "", reason);
    }

    public static BotAgentChatResult failed(String conversationId, String messageId, String errorMessage) {
        return new BotAgentChatResult(BotAgentChatStatus.FAILED, conversationId, messageId, "", errorMessage);
    }

    public BotAgentChatStatus getStatus() {
        return status;
    }

    public String getConversationId() {
        return conversationId;
    }

    public String getMessageId() {
        return messageId;
    }

    public String getReplyText() {
        return replyText;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
