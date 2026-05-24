package com.wens.breeding.graph;

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

class RuleBasedAnalysisGraphTest {
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
    private final RuleBasedAnalysisGraph graph = new RuleBasedAnalysisGraph(baseClient, baseClient, baseClient);

    @Test
    void routesWeightTrendAnalysisAndWritesResult() {
        AnalysisResult result = graph.run(request("REQ-G-001", AnalysisType.WEIGHT_TREND));

        assertEquals(RiskLevel.HIGH, result.getRiskLevel());
        assertTrue(baseClient.findWrittenAnalysisRequest("REQ-G-001").isPresent());
        assertTrue(baseClient.findWrittenAnalysisResult("REQ-G-001").isPresent());
    }

    @Test
    void routesUniformityAnalysis() {
        AnalysisResult result = graph.run(request("REQ-G-002", AnalysisType.UNIFORMITY));

        assertEquals(RiskLevel.HIGH, result.getRiskLevel());
        assertTrue(result.getSummary().contains("uniformity"));
    }

    @Test
    void routesFcrAnalysis() {
        AnalysisResult result = graph.run(request("REQ-G-003", AnalysisType.FEED_CONVERSION_RATIO));

        assertEquals(RiskLevel.HIGH, result.getRiskLevel());
        assertTrue(result.getSummary().contains("FCR"));
    }

    @Test
    void returnsUnknownWhenBatchIsMissing() {
        AnalysisResult result = graph.run(new AnalysisRequest(
                "REQ-G-004",
                RequestSource.MANUAL_TEST,
                "ou_test",
                "MISSING",
                AnalysisType.WEIGHT_TREND,
                LocalDate.parse("2026-05-20"),
                LocalDate.parse("2026-05-22"),
                "Analyze missing batch"));

        assertEquals(RiskLevel.UNKNOWN, result.getRiskLevel());
        assertTrue(result.getSummary().contains("was not found"));
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
