package com.wens.breeding.app.baseapp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.Test;

import com.wens.breeding.analysis.model.BreedingBatch;
import com.wens.breeding.analysis.model.BreedingStandard;
import com.wens.breeding.analysis.model.FcrRecord;
import com.wens.breeding.analysis.model.FcrStandard;
import com.wens.breeding.analysis.model.RiskLevel;
import com.wens.breeding.analysis.model.WeightRecord;
import com.wens.breeding.graph.RuleBasedAnalysisGraph;
import com.wens.breeding.lark.base.InMemoryBreedingBaseClient;
import com.wens.breeding.visualization.WeightTrendVisualizationGenerator;

class ApplicationAnalysisServiceTest {
    @Test
    void runsWeightTrendAnalysisAndWritesVisualizationData() {
        InMemoryBreedingBaseClient baseClient = baseClient();
        ApplicationAnalysisService service = new ApplicationAnalysisService(
                new RuleBasedAnalysisGraph(baseClient, baseClient, baseClient),
                baseClient,
                baseClient,
                new WeightTrendVisualizationGenerator());

        BaseAppAnalysisResponse response = service.submit(request("WEIGHT_TREND"));

        assertEquals("REQ-APP-001", response.getRequestId());
        assertEquals("COMPLETED", response.getStatus());
        assertEquals(RiskLevel.HIGH, response.getRiskLevel());
        assertEquals(3, response.getVisualizationRecordIds().size());
        assertTrue(baseClient.findWrittenAnalysisRequest("REQ-APP-001").isPresent());
        assertTrue(baseClient.findWrittenAnalysisResult("REQ-APP-001").isPresent());
    }

    @Test
    void skipsVisualizationForNonWeightTrendAnalysis() {
        InMemoryBreedingBaseClient baseClient = baseClient();
        ApplicationAnalysisService service = new ApplicationAnalysisService(
                new RuleBasedAnalysisGraph(baseClient, baseClient, baseClient),
                baseClient,
                baseClient,
                new WeightTrendVisualizationGenerator());

        BaseAppAnalysisResponse response = service.submit(request("UNIFORMITY"));

        assertEquals(RiskLevel.HIGH, response.getRiskLevel());
        assertTrue(response.getVisualizationRecordIds().isEmpty());
    }

    private static BaseAppAnalysisRequest request(String analysisType) {
        BaseAppAnalysisRequest request = new BaseAppAnalysisRequest();
        request.setRequestId("REQ-APP-001");
        request.setRequesterOpenId("ou_test");
        request.setBatchId("BATCH-001");
        request.setAnalysisType(analysisType);
        request.setStartDate("2026-05-20");
        request.setEndDate("2026-05-22");
        request.setRawQuestion("Analyze BATCH-001");
        return request;
    }

    private static InMemoryBreedingBaseClient baseClient() {
        return new InMemoryBreedingBaseClient(
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
