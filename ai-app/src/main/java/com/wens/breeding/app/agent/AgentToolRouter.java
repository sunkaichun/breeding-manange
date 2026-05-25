package com.wens.breeding.app.agent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Component;

@Component
public final class AgentToolRouter {
    private final List<AgentTool> tools;

    public AgentToolRouter(List<AgentTool> tools) {
        this.tools = tools == null ? Collections.emptyList() : List.copyOf(tools);
    }

    public List<AgentTool> route(AgentToolRequest request) {
        List<AgentTool> matchedTools = new ArrayList<>();
        for (AgentTool tool : tools) {
            if (tool.supports(request)) {
                matchedTools.add(tool);
            }
        }
        return matchedTools;
    }
}
