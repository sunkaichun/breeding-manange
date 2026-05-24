package com.wens.breeding.visualization;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.wens.breeding.analysis.model.AnalysisRequest;
import com.wens.breeding.analysis.model.AnalysisType;
import com.wens.breeding.analysis.model.BreedingBatch;
import com.wens.breeding.analysis.model.BreedingStandard;
import com.wens.breeding.analysis.model.RequestSource;
import com.wens.breeding.analysis.model.WeightRecord;
import com.wens.breeding.lark.base.VisualizationDataRecord;

class WeightTrendVisualizationGeneratorTest {
    private final WeightTrendVisualizationGenerator generator = new WeightTrendVisualizationGenerator();

    @Test
    void generatesTrendBandAndAnomalyRecords() {
        List<VisualizationDataRecord> records = generator.generate(
                request(),
                batch(),
                Arrays.asList(
                        weight("2026-05-21", 51, "1.42"),
                        weight("2026-05-20", 50, "1.20"),
                        weight("2026-05-22", 52, "1.70")),
                java.util.Collections.singletonList(standard()));

        assertEquals(3, records.size());
        assertEquals("line", records.get(0).getChartType());
        assertTrue(records.get(0).getDataJson().contains("\"date\":\"2026-05-20\""));
        assertTrue(records.get(1).getDataJson().contains("\"min\":1.3"));
        assertTrue(records.get(2).getDataJson().contains("BELOW_STANDARD"));
        assertTrue(records.get(2).getDataJson().contains("ABOVE_STANDARD"));
    }

    @Test
    void marksMissingStandardAsAnomaly() {
        List<VisualizationDataRecord> records = generator.generate(
                request(),
                batch(),
                java.util.Collections.singletonList(weight("2026-05-20", 50, "1.42")),
                java.util.Collections.emptyList());

        assertTrue(records.get(1).getDataJson().equals("[]"));
        assertTrue(records.get(2).getDataJson().contains("MISSING_STANDARD"));
    }

    private static AnalysisRequest request() {
        return new AnalysisRequest(
                "REQ-VIZ-001",
                RequestSource.MANUAL_TEST,
                "ou_test",
                "BATCH-001",
                AnalysisType.WEIGHT_TREND,
                LocalDate.parse("2026-05-20"),
                LocalDate.parse("2026-05-22"),
                "Visualize weight trend");
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

    private static WeightRecord weight(String measuredDate, int ageDays, String averageWeightKg) {
        return new WeightRecord(
                "BATCH-001",
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
}
