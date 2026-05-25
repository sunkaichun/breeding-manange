package com.wens.breeding.graph.execution;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.Test;

import com.wens.breeding.analysis.model.AnalysisRequest;
import com.wens.breeding.analysis.model.AnalysisResult;
import com.wens.breeding.analysis.model.AnalysisType;
import com.wens.breeding.analysis.model.BreedingBatch;
import com.wens.breeding.analysis.model.BreedingStandard;
import com.wens.breeding.analysis.model.FcrRecord;
import com.wens.breeding.analysis.model.FcrStandard;
import com.wens.breeding.analysis.model.RequestSource;
import com.wens.breeding.analysis.model.RiskLevel;
import com.wens.breeding.analysis.model.WeightRecord;
import com.wens.breeding.lark.base.InMemoryBreedingBaseClient;

class NodeBasedAiExecutionEngineTest {
    private final InMemoryBreedingBaseClient baseClient = new InMemoryBreedingBaseClient(
            Collections.singletonList(batch()),
            Arrays.asList(
                    weight("2026-05-20", 50, "1.42", "82"),
                    weight("2026-05-21", 51, "1.20", "76"),
                    weight("2026-05-22", 52, "1.22", "77")),
            Collections.singletonList(weightStandard()),
            Arrays.asList(
                    fcr("2026-05-21", 51, "150", "80"),
                    fcr("2026-05-22", 52, "152", "80")),
            Collections.singletonList(fcrStandard()));

    @Test
    void executesAnalysisAsTraceableNodeGraph() {
        NodeBasedAiExecutionEngine engine = BreedingAnalysisExecutionGraphFactory
                .langGraphStyle(baseClient, baseClient, baseClient);

        AiExecutionResult result = engine.execute(request("REQ-N-001", AnalysisType.WEIGHT_TREND));

        assertEquals(AiFramework.LANGGRAPH4J, result.getFramework());
        assertEquals(RiskLevel.HIGH, result.getAnalysisResult().getRiskLevel());
        assertEquals(4, result.getTraces().size());
        assertEquals("persist-analysis-request", result.getTraces().get(0).getNodeName());
        assertEquals("load-batch", result.getTraces().get(1).getNodeName());
        assertEquals("rule-analysis", result.getTraces().get(2).getNodeName());
        assertEquals("persist-analysis-result", result.getTraces().get(3).getNodeName());
        assertTrue(baseClient.findWrittenAnalysisRequest("REQ-N-001").isPresent());
        assertTrue(baseClient.findWrittenAnalysisResult("REQ-N-001").isPresent());
    }

    @Test
    void persistsMissingBatchFallbackResult() {
        NodeBasedAiExecutionEngine engine = BreedingAnalysisExecutionGraphFactory
                .langGraphStyle(baseClient, baseClient, baseClient);

        AiExecutionResult result = engine.execute(new AnalysisRequest(
                "REQ-N-002",
                RequestSource.MANUAL_TEST,
                "ou_test",
                "MISSING",
                AnalysisType.WEIGHT_TREND,
                LocalDate.parse("2026-05-20"),
                LocalDate.parse("2026-05-22"),
                "Analyze missing batch"));

        assertEquals(RiskLevel.UNKNOWN, result.getAnalysisResult().getRiskLevel());
        assertEquals(AiExecutionNodeStatus.SKIPPED, result.getTraces().get(2).getStatus());
        assertTrue(baseClient.findWrittenAnalysisResult("REQ-N-002").isPresent());
    }

    @Test
    void nativeLangGraph4jEngineRunsAnalysisGraph() {
        NativeLangGraph4jExecutionEngine engine = BreedingAnalysisExecutionGraphFactory
                .nativeLangGraph4j(baseClient, baseClient, baseClient);

        AiExecutionResult result = engine.execute(request("REQ-N-003", AnalysisType.UNIFORMITY));

        assertEquals(AiFramework.LANGGRAPH4J, result.getFramework());
        assertEquals(RiskLevel.HIGH, result.getAnalysisResult().getRiskLevel());
        assertEquals(4, result.getTraces().size());
        assertEquals("persist-analysis-request", result.getTraces().get(0).getNodeName());
        assertEquals("load-batch", result.getTraces().get(1).getNodeName());
        assertEquals("rule-analysis", result.getTraces().get(2).getNodeName());
        assertEquals("persist-analysis-result", result.getTraces().get(3).getNodeName());
        assertTrue(baseClient.findWrittenAnalysisRequest("REQ-N-003").isPresent());
        assertTrue(baseClient.findWrittenAnalysisResult("REQ-N-003").isPresent());
    }

    @Test
    void exposesFrameworkIntegrationPlan() {
        AiFrameworkRegistry registry = new AiFrameworkRegistry();

        assertEquals(4, registry.listIntegrations().size());
        assertEquals(AiFramework.LANGGRAPH4J, registry.listIntegrations().get(0).getFramework());
        assertTrue(registry.listIntegrations().get(0).getAdapterClassName().contains("NativeLangGraph4j"));
        assertTrue(registry.listIntegrations().get(0).getRequiredJavaVersion().contains("21"));
        assertTrue(registry.listIntegrations().get(1).getAdapterClassName().contains("LangChain4j"));
        assertEquals(AiFramework.SPRING_AI, registry.listIntegrations().get(2).getFramework());
        assertTrue(registry.listIntegrations().get(2).getAdapterClassName().contains("SpringAi"));
    }

    private static AnalysisRequest request(String requestId, AnalysisType analysisType) {
        return new AnalysisRequest(
                requestId,
                RequestSource.MANUAL_TEST,
                "ou_test",
                "BATCH-001",
                analysisType,
                LocalDate.parse("2026-05-20"),
                LocalDate.parse("2026-05-22"),
                "Analyze batch");
    }

    private static BreedingBatch batch() {
        return new BreedingBatch(
                "BATCH-001",
                "Org-A",
                "Datu2",
                "Mixed",
                LocalDate.parse("2026-04-01"),
                LocalDate.parse("2026-06-20"),
                "ou_test",
                "Owner");
    }

    private static WeightRecord weight(String measuredDate, int ageDays, String averageWeightKg, String uniformityPercent) {
        return new WeightRecord(
                "BATCH-001",
                LocalDate.parse(measuredDate),
                ageDays,
                new BigDecimal(averageWeightKg),
                new BigDecimal(uniformityPercent),
                1000);
    }

    private static BreedingStandard weightStandard() {
        return new BreedingStandard(
                "Datu2",
                "Mixed",
                31,
                60,
                new BigDecimal("1.30"),
                new BigDecimal("1.58"),
                new BigDecimal("80"));
    }

    private static FcrRecord fcr(String recordDate, int ageDays, String feedConsumedKg, String weightGainKg) {
        return new FcrRecord(
                "BATCH-001",
                LocalDate.parse(recordDate),
                ageDays,
                new BigDecimal(feedConsumedKg),
                new BigDecimal(weightGainKg));
    }

    private static FcrStandard fcrStandard() {
        return new FcrStandard(
                "Datu2",
                "Mixed",
                31,
                60,
                new BigDecimal("1.70"));
    }
}
