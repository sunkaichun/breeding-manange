package com.wens.breeding.graph.execution;

import java.io.Serializable;

public final class AiExecutionTrace implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String nodeName;
    private final AiExecutionNodeStatus status;
    private final String message;

    public AiExecutionTrace(String nodeName, AiExecutionNodeStatus status, String message) {
        this.nodeName = requireText(nodeName, "nodeName");
        this.status = status == null ? AiExecutionNodeStatus.COMPLETED : status;
        this.message = message == null ? "" : message;
    }

    public String getNodeName() {
        return nodeName;
    }

    public AiExecutionNodeStatus getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value;
    }
}
