package com.wens.breeding.lark.base;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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

class InMemoryBreedingBaseClientTest {
    private final InMemoryBreedingBaseClient client = new InMemoryBreedingBaseClient(
            Collections.singletonList(batch("BATCH-001")),
            Arrays.asList(
                    weight("BATCH-001", "2026-05-20", 50, "1.42"),
                    weight("BATCH-001", "2026-05-21", 51, "1.45"),
                    weight("BATCH-001", "2026-05-22", 52, "1.48"),
                    weight("BATCH-002", "2026-05-22", 52, "1.40")),
            Collections.singletonList(standard()),
            Arrays.asList(
                    fcr("BATCH-001", "2026-05-21", 51, "120", "80"),
                    fcr("BATCH-001", "2026-05-22", 52, "152", "80"),
                    fcr("BATCH-002", "2026-05-22", 52, "120", "80")),
            Collections.singletonList(fcrStandard()));

    @Test
    void findsBatchById() {
        assertTrue(client.findBatchById("BATCH-001").isPresent());
        assertFalse(client.findBatchById("UNKNOWN").isPresent());
    }

    @Test
    void listsWeightRecordsByBatchAndDateRange() {
        List<WeightRecord> records = client.listWeightRecords(
                "BATCH-001",
                LocalDate.parse("2026-05-21"),
                LocalDate.parse("2026-05-22"));

        assertEquals(2, records.size());
        assertEquals(LocalDate.parse("2026-05-21"), records.get(0).getMeasuredDate());
        assertEquals(LocalDate.parse("2026-05-22"), records.get(1).getMeasuredDate());
    }

    @Test
    void findsStandardByBreedFeedingModeAndAgeRange() {
        assertTrue(client.findStandard("Datu2", "Mixed", 53).isPresent());
        assertFalse(client.findStandard("Datu2", "Mixed", 61).isPresent());
    }

    @Test
    void listsFcrRecordsAndFindsFcrStandard() {
        List<FcrRecord> records = client.listFcrRecords(
                "BATCH-001",
                LocalDate.parse("2026-05-21"),
                LocalDate.parse("2026-05-22"));

        assertEquals(2, records.size());
        assertTrue(client.findFcrStandard("Datu2", "Mixed", 53).isPresent());
        assertFalse(client.findFcrStandard("Datu2", "Mixed", 61).isPresent());
    }

    @Test
    void rejectsInvalidDateRange() {
        assertThrows(IllegalArgumentException.class, () -> client.listWeightRecords(
                "BATCH-001",
                LocalDate.parse("2026-05-22"),
                LocalDate.parse("2026-05-21")));
    }

    @Test
    void writesAnalysisRequestResultAndVisualizationData() {
        AnalysisRequest request = new AnalysisRequest(
                "REQ-001",
                RequestSource.LARK_BOT,
                "ou_test",
                "BATCH-001",
                AnalysisType.WEIGHT_TREND,
                LocalDate.parse("2026-05-20"),
                LocalDate.parse("2026-05-22"),
                "Analyze BATCH-001 weight trend");
        AnalysisResult result = new AnalysisResult(
                "REQ-001",
                RiskLevel.LOW,
                "Weight trend is within the configured range.",
                Collections.singletonList("Average weight is inside the standard range."),
                Collections.singletonList("Keep current feeding plan."));
        VisualizationDataRecord visualizationData = new VisualizationDataRecord(
                "REQ-001",
                "line",
                "measuredDate",
                "averageWeightKg",
                "[{\"date\":\"2026-05-22\",\"value\":1.48}]");

        String requestRecordId = client.createAnalysisRequest(request);
        String resultRecordId = client.saveAnalysisResult(result);
        String visualizationRecordId = client.saveVisualizationData(visualizationData);

        assertEquals("REQ-001", requestRecordId);
        assertEquals("REQ-001", resultRecordId);
        assertTrue(client.findWrittenAnalysisRequest("REQ-001").isPresent());
        assertTrue(client.findWrittenAnalysisResult("REQ-001").isPresent());
        assertTrue(client.findWrittenVisualizationData(visualizationRecordId).isPresent());
    }

    private static BreedingBatch batch(String batchId) {
        return new BreedingBatch(
                batchId,
                "Org-A",
                "Datu2",
                "Mixed",
                LocalDate.parse("2026-04-01"),
                LocalDate.parse("2026-06-20"),
                "ou_test",
                "Owner");
    }

    private static WeightRecord weight(String batchId, String measuredDate, int ageDays, String averageWeightKg) {
        return new WeightRecord(
                batchId,
                LocalDate.parse(measuredDate),
                ageDays,
                new BigDecimal(averageWeightKg),
                new BigDecimal("82"),
                1000);
    }

    private static BreedingStandard standard() {
        return new BreedingStandard(
                "Datu2",
                "Mixed",
                31,
                60,
                new BigDecimal("1.30"),
                new BigDecimal("1.58"),
                new BigDecimal("80"));
    }

    private static FcrRecord fcr(String batchId, String recordDate, int ageDays, String feedConsumedKg, String weightGainKg) {
        return new FcrRecord(
                batchId,
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
