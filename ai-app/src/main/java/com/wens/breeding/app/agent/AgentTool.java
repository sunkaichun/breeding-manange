package com.wens.breeding.app.agent;

public interface AgentTool {
    String name();

    String description();

    boolean supports(AgentToolRequest request);

    AgentToolResult execute(AgentToolRequest request);
}
