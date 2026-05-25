package com.wens.breeding.lark.bot;

import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import com.wens.breeding.analysis.model.AnalysisResult;
import com.wens.breeding.lark.bot.command.BotCommandRouter;
import com.wens.breeding.lark.bot.command.RuleBasedBotCommandRouter;
import com.wens.breeding.lark.bot.event.BotMessageEventHandler;
import com.wens.breeding.lark.bot.queue.QueuedBotMessageEventHandler;
import com.wens.breeding.lark.bot.runner.LarkEventConsumerRunner;
import com.wens.breeding.lark.bot.runner.ProcessBuilderLarkEventConsumerProcessLauncher;
import com.wens.breeding.lark.bot.workflow.BotAnalysisRequestFactory;
import com.wens.breeding.task.AsyncTaskQueue;
import com.wens.breeding.task.InMemoryTaskStore;
import com.wens.breeding.task.TaskStore;

public final class LarkBotLongConnectionAdapterModule {
    private LarkBotLongConnectionAdapterModule() {
    }

    public static String name() {
        return "lark-bot-long-connection-adapter";
    }

    public static LarkEventConsumerRunner defaultRunner() {
        return new LarkEventConsumerRunner(new ProcessBuilderLarkEventConsumerProcessLauncher());
    }

    public static BotCommandRouter defaultCommandRouter() {
        return new RuleBasedBotCommandRouter();
    }

    public static BotAnalysisRequestFactory defaultAnalysisRequestFactory() {
        return new BotAnalysisRequestFactory();
    }

    public static TaskStore<AnalysisResult> defaultAnalysisTaskStore() {
        return new InMemoryTaskStore<>();
    }

    public static AsyncTaskQueue<AnalysisResult> defaultAnalysisTaskQueue(TaskStore<AnalysisResult> taskStore) {
        return new AsyncTaskQueue<>(taskStore, defaultAnalysisTaskExecutor());
    }

    public static ExecutorService defaultAnalysisTaskExecutor() {
        return Executors.newFixedThreadPool(4);
    }

    public static QueuedBotMessageEventHandler defaultQueuedMessageHandler(
            BotMessageEventHandler delegate,
            Duration delay) {
        return new QueuedBotMessageEventHandler(delegate, defaultBotMessageQueueExecutor(), delay, true);
    }

    public static ScheduledExecutorService defaultBotMessageQueueExecutor() {
        return Executors.newScheduledThreadPool(2);
    }
}
