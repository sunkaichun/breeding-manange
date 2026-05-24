package com.wens.breeding.lark.bot.workflow;

import java.util.Objects;

import com.wens.breeding.analysis.model.AnalysisRequest;
import com.wens.breeding.analysis.model.AnalysisResult;
import com.wens.breeding.graph.AnalysisGraph;
import com.wens.breeding.lark.bot.command.BotCommand;
import com.wens.breeding.lark.bot.command.BotCommandRouter;
import com.wens.breeding.lark.bot.command.BotCommandType;
import com.wens.breeding.lark.bot.event.BotMessageEvent;
import com.wens.breeding.lark.bot.event.BotMessageEventHandler;

public final class BotAnalysisWorkflow implements BotMessageEventHandler {
    private final BotCommandRouter commandRouter;
    private final BotAnalysisRequestFactory requestFactory;
    private final AnalysisGraph analysisGraph;
    private BotAnalysisTaskResult lastResult;

    public BotAnalysisWorkflow(
            BotCommandRouter commandRouter,
            BotAnalysisRequestFactory requestFactory,
            AnalysisGraph analysisGraph) {
        this.commandRouter = Objects.requireNonNull(commandRouter, "commandRouter");
        this.requestFactory = Objects.requireNonNull(requestFactory, "requestFactory");
        this.analysisGraph = Objects.requireNonNull(analysisGraph, "analysisGraph");
    }

    @Override
    public void onEvent(BotMessageEvent event) {
        lastResult = handle(event);
    }

    public BotAnalysisTaskResult handle(BotMessageEvent event) {
        Objects.requireNonNull(event, "event");
        BotCommand command = commandRouter.route(event);
        if (command.getCommandType() != BotCommandType.ANALYSIS) {
            return BotAnalysisTaskResult.skipped(command, "Command does not trigger an analysis graph");
        }

        AnalysisRequest request = requestFactory.create(event, command);
        try {
            AnalysisResult result = analysisGraph.run(request);
            return BotAnalysisTaskResult.completed(command, result);
        } catch (RuntimeException exception) {
            return BotAnalysisTaskResult.failed(command, request.getRequestId(), exception.getMessage());
        }
    }

    public BotAnalysisTaskResult getLastResult() {
        return lastResult;
    }
}
