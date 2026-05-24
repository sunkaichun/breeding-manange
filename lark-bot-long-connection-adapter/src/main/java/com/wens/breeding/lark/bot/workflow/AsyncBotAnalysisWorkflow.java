package com.wens.breeding.lark.bot.workflow;

import java.util.Objects;
import java.util.Optional;

import com.wens.breeding.analysis.model.AnalysisRequest;
import com.wens.breeding.analysis.model.AnalysisResult;
import com.wens.breeding.graph.AnalysisGraph;
import com.wens.breeding.lark.bot.command.BotCommand;
import com.wens.breeding.lark.bot.command.BotCommandRouter;
import com.wens.breeding.lark.bot.command.BotCommandType;
import com.wens.breeding.lark.bot.event.BotMessageEvent;
import com.wens.breeding.lark.bot.event.BotMessageEventHandler;
import com.wens.breeding.lark.im.LarkImClient;
import com.wens.breeding.task.AsyncTaskQueue;
import com.wens.breeding.task.TaskRecord;
import com.wens.breeding.task.TaskStore;

public final class AsyncBotAnalysisWorkflow implements BotMessageEventHandler {
    private final BotCommandRouter commandRouter;
    private final BotAnalysisRequestFactory requestFactory;
    private final AnalysisGraph analysisGraph;
    private final LarkImClient imClient;
    private final AsyncTaskQueue<AnalysisResult> taskQueue;
    private final TaskStore<AnalysisResult> taskStore;
    private BotAnalysisTaskResult lastResult;

    public AsyncBotAnalysisWorkflow(
            BotCommandRouter commandRouter,
            BotAnalysisRequestFactory requestFactory,
            AnalysisGraph analysisGraph,
            LarkImClient imClient,
            AsyncTaskQueue<AnalysisResult> taskQueue,
            TaskStore<AnalysisResult> taskStore) {
        this.commandRouter = Objects.requireNonNull(commandRouter, "commandRouter");
        this.requestFactory = Objects.requireNonNull(requestFactory, "requestFactory");
        this.analysisGraph = Objects.requireNonNull(analysisGraph, "analysisGraph");
        this.imClient = Objects.requireNonNull(imClient, "imClient");
        this.taskQueue = Objects.requireNonNull(taskQueue, "taskQueue");
        this.taskStore = Objects.requireNonNull(taskStore, "taskStore");
    }

    @Override
    public void onEvent(BotMessageEvent event) {
        lastResult = handle(event);
    }

    public BotAnalysisTaskResult handle(BotMessageEvent event) {
        Objects.requireNonNull(event, "event");
        BotCommand command = commandRouter.route(event);
        if (command.getCommandType() != BotCommandType.ANALYSIS) {
            return BotAnalysisTaskResult.skipped(command, "Command does not trigger an async analysis task");
        }

        AnalysisRequest request = requestFactory.create(event, command);
        String acceptedMessage = acceptedMessage(request);
        imClient.sendText(event.getChatId(), acceptedMessage);
        taskQueue.submit(request.getRequestId(), event.getMessageId(), () -> analysisGraph.run(request));
        return BotAnalysisTaskResult.accepted(command, request.getRequestId(), acceptedMessage);
    }

    public Optional<TaskRecord<AnalysisResult>> findTask(String requestId) {
        return taskStore.findByTaskId(requestId);
    }

    public BotAnalysisTaskResult getLastResult() {
        return lastResult;
    }

    private static String acceptedMessage(AnalysisRequest request) {
        return "Analysis task accepted: " + request.getRequestId() + ". Result will be sent after completion.";
    }
}
