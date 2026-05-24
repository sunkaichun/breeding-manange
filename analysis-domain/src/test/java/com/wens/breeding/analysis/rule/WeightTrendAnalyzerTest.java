package com.wens.breeding.analysis.rule;

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
import com.wens.breeding.analysis.model.RequestSource;
import com.wens.breeding.analysis.model.RiskLevel;
import com.wens.breeding.analysis.model.WeightRecord;

class WeightTrendAnalyzerTest {
    private final WeightTrendAnalyzer analyzer = new WeightTrendAnalyzer();

    @Test
    void returnsLowRiskWhenAllRecordsMatchStandard() {
        AnalysisResult result = analyzer.analyze(
                request(),
                batch(),
                Arrays.asList(weight("2026-05-20", 50, "1.42"), weight("2026-05-21", 51, "1.45")),
                Collections.singletonList(standard()));

        assertEquals(RiskLevel.LOW, result.getRiskLevel());
        assertTrue(result.getSummary().contains("Below-standard days: 0"));
    }

    @Test
    void returnsHighRiskWhenWeightIsConsecutivelyBelowStandard() {
        AnalysisResult result = analyzer.analyze(
                request(),
                batch(),
                Arrays.asList(weight("2026-05-20", 50, "1.20"), weight("2026-05-21", 51, "1.22")),
                Collections.singletonList(standard()));

        assertEquals(RiskLevel.HIGH, result.getRiskLevel());
        assertTrue(result.getReasons().get(0).contains("below standard range"));
    }

    @Test
    void returnsUnknownRiskWhenNoStandardMatches() {
        AnalysisResult result = analyzer.analyze(
                request(),
                batch(),
                Collections.singletonList(weight("2026-05-20", 50, "1.42")),
                Collections.emptyList());

        assertEquals(RiskLevel.UNKNOWN, result.getRiskLevel());
        assertTrue(result.getSummary().contains("missing-standard days: 1"));
    }

    private static AnalysisRequest request() {
        return new AnalysisRequest(
                "REQ-001",
                RequestSource.MANUAL_TEST,
                "ou_test",
                "BATCH-001",
                AnalysisType.WEIGHT_TREND,
                LocalDate.parse("2026-05-20"),
                LocalDate.parse("2026-05-21"),
                "Analyze weight trend");
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
