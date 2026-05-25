package com.wens.breeding.lark.bot.chat;

public interface BotAgentChatEventSink {
    void onToken(String token);

    void onToolCall(String toolName);

    void onToolResult(String toolName, String summary);
}
