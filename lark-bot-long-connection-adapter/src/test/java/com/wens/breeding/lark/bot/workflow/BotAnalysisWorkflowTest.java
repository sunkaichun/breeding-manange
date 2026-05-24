package com.wens.breeding.lark.bot.workflow;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Collections;

import org.junit.jupiter.api.Test;

import com.wens.breeding.analysis.model.AnalysisRequest;
import com.wens.breeding.analysis.model.AnalysisResult;
import com.wens.breeding.analysis.model.AnalysisType;
import com.wens.breeding.analysis.model.RequestSource;
import com.wens.breeding.analysis.model.RiskLevel;
import com.wens.breeding.graph.AnalysisGraph;
import com.wens.breeding.lark.bot.command.RuleBasedBotCommandRouter;
import com.wens.breeding.lark.bot.event.BotMessageEvent;

class BotAnalysisWorkflowTest {
    private final Clock clock = Clock.fixed(Instant.parse("2026-05-24T00:00:00Z"), ZoneOffset.UTC);

    @Test
    void analysisCommandCreatesRequestAndRunsGraph() {
        CapturingAnalysisGraph graph = new CapturingAnalysisGraph();
        BotAnalysisWorkflow workflow = workflow(graph);

        BotAnalysisTaskResult result = workflow.handle(event("analyze BATCH-001 weight for recent 7 days"));

        assertEquals(BotAnalysisTaskStatus.COMPLETED, result.getStatus());
        assertEquals("BOT-evt-001", result.getRequestId());
        assertNotNull(result.getAnalysisResult());
        assertEquals("BOT-evt-001", graph.request.getRequestId());
        assertEquals(RequestSource.LARK_BOT, graph.request.getSource());
        assertEquals("ou_sender", graph.request.getRequesterOpenId());
        assertEquals("BATCH-001", graph.request.getBatchId());
        assertEquals(AnalysisType.WEIGHT_TREND, graph.request.getAnalysisType());
        assertEquals("2026-05-18", graph.request.getStartDate().toString());
        assertEquals("2026-05-24", graph.request.getEndDate().toString());
    }

    @Test
    void nonAnalysisCommandIsSkipped() {
        CapturingAnalysisGraph graph = new CapturingAnalysisGraph();
        BotAnalysisWorkflow workflow = workflow(graph);

        BotAnalysisTaskResult result = workflow.handle(event("help"));

        assertEquals(BotAnalysisTaskStatus.SKIPPED, result.getStatus());
        assertEquals(null, graph.request);
    }

    @Test
    void recordsFailedGraphRun() {
        BotAnalysisWorkflow workflow = workflow(request -> {
            throw new IllegalStateException("graph failed");
        });

        BotAnalysisTaskResult result = workflow.handle(event("analyze BATCH-001 FCR for recent 14 days"));

        assertEquals(BotAnalysisTaskStatus.FAILED, result.getStatus());
        assertEquals("BOT-evt-001", result.getRequestId());
        assertEquals("graph failed", result.getMessage());
    }

    private BotAnalysisWorkflow workflow(AnalysisGraph graph) {
        return new BotAnalysisWorkflow(
                new RuleBasedBotCommandRouter(),
                new BotAnalysisRequestFactory(clock),
                graph);
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
        private AnalysisRequest request;

        @Override
        public AnalysisResult run(AnalysisRequest request) {
            this.request = request;
            return new AnalysisResult(
                    request.getRequestId(),
                    RiskLevel.LOW,
                    "Analysis completed",
                    Collections.singletonList("Graph received the request."),
                    Collections.singletonList("Continue monitoring."));
        }
    }
}
