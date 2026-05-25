package com.wens.breeding.app.agent;

import java.util.Map;

public interface AgentEventSink {
    void onToken(String token);

    void onToolCall(String toolName, Map<String, Object> arguments);

    void onToolResult(AgentToolResult result);
}
