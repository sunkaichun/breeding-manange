package com.wens.breeding.app.agent;

public interface AgentChatClient {
    void stream(AgentChatPrompt prompt, AgentTokenSink tokenSink);
}
