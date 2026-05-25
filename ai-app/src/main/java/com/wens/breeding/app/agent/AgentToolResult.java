package com.wens.breeding.app.agent;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class AgentToolResult {
    private final String toolName;
    private final String summary;
    private final Map<String, Object> arguments;
    private final Map<String, Object> data;

    public AgentToolResult(
            String toolName,
            String summary,
            Map<String, Object> arguments,
            Map<String, Object> data) {
        this.toolName = requireText(toolName, "toolName");
        this.summary = summary == null ? "" : summary;
        this.arguments = immutableCopy(arguments);
        this.data = immutableCopy(data);
    }

    public String getToolName() {
        return toolName;
    }

    public String getSummary() {
        return summary;
    }

    public Map<String, Object> getArguments() {
        return arguments;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public String toContextBlock() {
        return "Tool: " + toolName + "\n"
                + "Arguments: " + arguments + "\n"
                + "Summary: " + summary + "\n"
                + "Data: " + data;
    }

    private static Map<String, Object> immutableCopy(Map<String, Object> values) {
        if (values == null || values.isEmpty()) {
            return Collections.emptyMap();
        }
        return Collections.unmodifiableMap(new LinkedHashMap<>(values));
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value;
    }
}
