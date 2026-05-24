package com.wens.breeding.lark.bot.workflow;

import com.wens.breeding.analysis.model.AnalysisResult;
import com.wens.breeding.lark.bot.command.BotCommand;

public final class BotAnalysisTaskResult {
    private final BotAnalysisTaskStatus status;
    private final BotCommand command;
    private final String requestId;
    private final AnalysisResult analysisResult;
    private final String message;

    public BotAnalysisTaskResult(
            BotAnalysisTaskStatus status,
            BotCommand command,
            String requestId,
            AnalysisResult analysisResult,
            String message) {
        this.status = status == null ? BotAnalysisTaskStatus.SKIPPED : status;
        this.command = command;
        this.requestId = requestId == null ? "" : requestId;
        this.analysisResult = analysisResult;
        this.message = message == null ? "" : message;
    }

    public static BotAnalysisTaskResult completed(BotCommand command, AnalysisResult result) {
        return new BotAnalysisTaskResult(
                BotAnalysisTaskStatus.COMPLETED,
                command,
                result.getRequestId(),
                result,
                "Analysis task completed");
    }

    public static BotAnalysisTaskResult accepted(BotCommand command, String requestId, String message) {
        return new BotAnalysisTaskResult(BotAnalysisTaskStatus.ACCEPTED, command, requestId, null, message);
    }

    public static BotAnalysisTaskResult skipped(BotCommand command, String message) {
        return new BotAnalysisTaskResult(BotAnalysisTaskStatus.SKIPPED, command, "", null, message);
    }

    public static BotAnalysisTaskResult failed(BotCommand command, String requestId, String message) {
        return new BotAnalysisTaskResult(BotAnalysisTaskStatus.FAILED, command, requestId, null, message);
    }

    public BotAnalysisTaskStatus getStatus() {
        return status;
    }

    public BotCommand getCommand() {
        return command;
    }

    public String getRequestId() {
        return requestId;
    }

    public AnalysisResult getAnalysisResult() {
        return analysisResult;
    }

    public String getMessage() {
        return message;
    }
}
