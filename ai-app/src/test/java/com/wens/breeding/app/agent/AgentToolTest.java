package com.wens.breeding.app.agent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.wens.breeding.analysis.model.AnalysisRequest;
import com.wens.breeding.analysis.model.AnalysisResult;
import com.wens.breeding.analysis.model.BreedingBatch;
import com.wens.breeding.analysis.model.BreedingStandard;
import com.wens.breeding.analysis.model.FcrRecord;
import com.wens.breeding.analysis.model.FcrStandard;
import com.wens.breeding.analysis.model.RiskLevel;
import com.wens.breeding.analysis.model.WeightRecord;
import com.wens.breeding.graph.AnalysisGraph;
import com.wens.breeding.lark.base.InMemoryBreedingBaseClient;

import org.junit.jupiter.api.Test;

class AgentToolTest {
    @Test
    void batchLookupReturnsBatchContext() {
        BatchLookupAgentTool tool = new BatchLookupAgentTool(baseClient());
        AgentToolRequest request = toolRequest("查询 BATCH-001 的基础信息");

        AgentToolResult result = tool.execute(request);

        assertTrue(tool.supports(request));
        assertEquals("batch_lookup", result.getToolName());
        assertEquals(true, result.getData().get("found"));
        assertEquals("BATCH-001", result.getData().get("batchId"));
    }

    @Test
    void batchLookupHandlesMissingBatch() {
        BatchLookupAgentTool tool = new BatchLookupAgentTool(baseClient());

        AgentToolResult result = tool.execute(toolRequest("查询 BATCH-404 的基础信息"));

        assertEquals(false, result.getData().get("found"));
        assertTrue(result.getSummary().contains("未找到"));
    }

    @Test
    void breedingAnalysisRunsGraphWithParsedArguments() {
        CapturingAnalysisGraph graph = new CapturingAnalysisGraph();
        BreedingAnalysisAgentTool tool = new BreedingAnalysisAgentTool(graph);
        AgentToolRequest request = toolRequest("帮我分析 BATCH-001 2026-05-20 到 2026-05-22 的体重趋势");

        AgentToolResult result = tool.execute(request);

        assertTrue(tool.supports(request));
        assertEquals("breeding_analysis", result.getToolName());
        assertEquals("BATCH-001", graph.lastRequest.getBatchId());
        assertEquals("WEIGHT_TREND", graph.lastRequest.getAnalysisType().name());
        assertEquals("HIGH", result.getData().get("riskLevel"));
    }

    @Test
    void routerDoesNotMatchPlainChat() {
        AgentToolRouter router = new AgentToolRouter(List.of(
                new BatchLookupAgentTool(baseClient()),
                new BreedingAnalysisAgentTool(new CapturingAnalysisGraph())));

        assertTrue(router.route(toolRequest("你好，介绍一下你能做什么")).isEmpty());
        assertFalse(router.route(toolRequest("查询 BATCH-001 的基础信息")).isEmpty());
    }

    private static AgentToolRequest toolRequest(String content) {
        AgentChatMessage message = new AgentChatMessage();
        message.setRole("user");
        message.setContent(content);
        return new AgentToolRequest("test", List.of(message), content);
    }

    private static InMemoryBreedingBaseClient baseClient() {
        return new InMemoryBreedingBaseClient(
                Collections.singletonList(new BreedingBatch(
                        "BATCH-001",
                        "Org-A",
                        "Datu2",
                        "Mixed",
                        LocalDate.parse("2026-04-01"),
                        LocalDate.parse("2026-06-20"),
                        "ou_test",
                        "Owner")),
                Arrays.asList(
                        new WeightRecord("BATCH-001", LocalDate.parse("2026-05-20"), 50, new BigDecimal("1.42"), new BigDecimal("82"), 1000),
                        new WeightRecord("BATCH-001", LocalDate.parse("2026-05-22"), 52, new BigDecimal("1.22"), new BigDecimal("77"), 1000)),
                Collections.singletonList(new BreedingStandard("Datu2", "Mixed", 31, 60, new BigDecimal("1.30"), new BigDecimal("1.58"), new BigDecimal("80"))),
                Collections.singletonList(new FcrRecord("BATCH-001", LocalDate.parse("2026-05-22"), 52, new BigDecimal("152"), new BigDecimal("80"))),
                Collections.singletonList(new FcrStandard("Datu2", "Mixed", 31, 60, new BigDecimal("1.70"))));
    }

    private static final class CapturingAnalysisGraph implements AnalysisGraph {
        private AnalysisRequest lastRequest;

        @Override
        public AnalysisResult run(AnalysisRequest request) {
            this.lastRequest = request;
            return new AnalysisResult(
                    request.getRequestId(),
                    RiskLevel.HIGH,
                    "Weight trend has high risk.",
                    List.of("Average weight is below standard."),
                    List.of("Review feeding plan."));
        }
    }
}
