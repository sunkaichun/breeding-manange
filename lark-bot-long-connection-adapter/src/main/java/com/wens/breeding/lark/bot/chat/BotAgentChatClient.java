package com.wens.breeding.lark.bot.chat;

public interface BotAgentChatClient {
    void stream(BotAgentChatRequest request, BotAgentChatEventSink eventSink);
}
