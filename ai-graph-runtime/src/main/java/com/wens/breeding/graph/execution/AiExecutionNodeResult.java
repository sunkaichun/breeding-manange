package com.wens.breeding.graph.execution;

public final class AiExecutionNodeResult {
    private final AiExecutionNodeStatus status;
    private final String message;

    private AiExecutionNodeResult(AiExecutionNodeStatus status, String message) {
        this.status = status == null ? AiExecutionNodeStatus.COMPLETED : status;
        this.message = message == null ? "" : message;
    }

    public static AiExecutionNodeResult completed(String message) {
        return new AiExecutionNodeResult(AiExecutionNodeStatus.COMPLETED, message);
    }

    public static AiExecutionNodeResult skipped(String message) {
        return new AiExecutionNodeResult(AiExecutionNodeStatus.SKIPPED, message);
    }

    public static AiExecutionNodeResult failed(String message) {
        return new AiExecutionNodeResult(AiExecutionNodeStatus.FAILED, message);
    }

    public AiExecutionNodeStatus getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}
