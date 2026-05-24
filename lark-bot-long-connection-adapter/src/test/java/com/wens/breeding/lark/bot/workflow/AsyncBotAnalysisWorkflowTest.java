package com.wens.breeding.lark.bot.workflow;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;

import org.junit.jupiter.api.Test;

import com.wens.breeding.analysis.model.AnalysisRequest;
import com.wens.breeding.analysis.model.AnalysisResult;
import com.wens.breeding.analysis.model.RiskLevel;
import com.wens.breeding.graph.AnalysisGraph;
import com.wens.breeding.lark.bot.command.RuleBasedBotCommandRouter;
import com.wens.breeding.lark.bot.event.BotMessageEvent;
import com.wens.breeding.lark.im.InMemoryLarkImClient;
import com.wens.breeding.task.AsyncTaskQueue;
import com.wens.breeding.task.InMemoryTaskStore;
import com.wens.breeding.task.TaskRecord;
import com.wens.breeding.task.TaskStatus;

class AsyncBotAnalysisWorkflowTest {
    private final Clock clock = Clock.fixed(Instant.parse("2026-05-24T00:00:00Z"), ZoneOffset.UTC);

    @Test
    void analysisCommandSendsAcceptedMessageBeforeGraphRuns() {
        InMemoryTaskStore<AnalysisResult> store = new InMemoryTaskStore<>();
        ManualExecutor executor = new ManualExecutor();
        InMemoryLarkImClient imClient = new InMemoryLarkImClient();
        CapturingAnalysisGraph graph = new CapturingAnalysisGraph();
        AsyncBotAnalysisWorkflow workflow = workflow(graph, imClient, store, executor);

        BotAnalysisTaskResult result = workflow.handle(event("analyze BATCH-001 weight for recent 7 days"));

        assertEquals(BotAnalysisTaskStatus.ACCEPTED, result.getStatus());
        assertEquals("BOT-evt-001", result.getRequestId());
        assertEquals(1, imClient.listSentMessages().size());
        assertEquals("oc_chat", imClient.listSentMessages().get(0).getChatId());
        assertTrue(imClient.listSentMessages().get(0).getText().contains("Analysis task accepted"));
        assertFalse(graph.ran);
        assertEquals(TaskStatus.ACCEPTED, store.findByTaskId("BOT-evt-001").get().getStatus());

        executor.runNext();

        TaskRecord<AnalysisResult> completed = store.findByTaskId("BOT-evt-001").get();
        assertTrue(graph.ran);
        assertEquals(TaskStatus.COMPLETED, completed.getStatus());
        assertEquals("BOT-evt-001", completed.getResult().getRequestId());
    }

    @Test
    void graphFailureMarksAsyncTaskFailed() {
        InMemoryTaskStore<AnalysisResult> store = new InMemoryTaskStore<>();
        ManualExecutor executor = new ManualExecutor();
        AsyncBotAnalysisWorkflow workflow = workflow(request -> {
            throw new IllegalStateException("graph failed");
        }, new InMemoryLarkImClient(), store, executor);

        workflow.handle(event("analyze BATCH-001 FCR for recent 14 days"));
        executor.runNext();

        TaskRecord<AnalysisResult> failed = store.findByTaskId("BOT-evt-001").get();
        assertEquals(TaskStatus.FAILED, failed.getStatus());
        assertEquals("graph failed", failed.getErrorMessage());
    }

    @Test
    void nonAnalysisCommandDoesNotSendAcceptedMessage() {
        InMemoryTaskStore<AnalysisResult> store = new InMemoryTaskStore<>();
        ManualExecutor executor = new ManualExecutor();
        InMemoryLarkImClient imClient = new InMemoryLarkImClient();
        AsyncBotAnalysisWorkflow workflow = workflow(new CapturingAnalysisGraph(), imClient, store, executor);

        BotAnalysisTaskResult result = workflow.handle(event("help"));

        assertEquals(BotAnalysisTaskStatus.SKIPPED, result.getStatus());
        assertEquals(0, imClient.listSentMessages().size());
        assertEquals(0, executor.size());
    }

    private AsyncBotAnalysisWorkflow workflow(
            AnalysisGraph graph,
            InMemoryLarkImClient imClient,
            InMemoryTaskStore<AnalysisResult> store,
            ManualExecutor executor) {
        return new AsyncBotAnalysisWorkflow(
                new RuleBasedBotCommandRouter(),
                new BotAnalysisRequestFactory(clock),
                graph,
                imClient,
                new AsyncTaskQueue<>(store, executor, clock),
                store);
    }

    private static BotMessageEvent event(String content) {
        return new BotMessageEvent(
                "evt-001",
                "im.message.receive_v1",
                "ou_sender",
                "oc_chat",
                "group",
                "om_message",
                "text",
                content,
                "1779616799000",
                "1779616800000");
    }

    private static final class CapturingAnalysisGraph implements AnalysisGraph {
        private boolean ran;

        @Override
        public AnalysisResult run(AnalysisRequest request) {
            ran = true;
            return new AnalysisResult(
                    request.getRequestId(),
                    RiskLevel.LOW,
                    "Analysis completed",
                    Collections.singletonList("Graph received the request."),
                    Collections.singletonList("Continue monitoring."));
        }
    }

    private static final class ManualExecutor implements Executor {
        private final List<Runnable> tasks = new ArrayList<>();

        @Override
        public void execute(Runnable command) {
            tasks.add(command);
        }

        private void runNext() {
            tasks.remove(0).run();
        }

        private int size() {
            return tasks.size();
        }
    }
}
