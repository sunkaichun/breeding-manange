package com.zhitian.breeding.analysis.rule;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.Test;

import com.zhitian.breeding.analysis.model.AnalysisRequest;
import com.zhitian.breeding.analysis.model.AnalysisResult;
import com.zhitian.breeding.analysis.model.AnalysisType;
import com.zhitian.breeding.analysis.model.BreedingBatch;
import com.zhitian.breeding.analysis.model.BreedingStandard;
import com.zhitian.breeding.analysis.model.RequestSource;
import com.zhitian.breeding.analysis.model.RiskLevel;
import com.zhitian.breeding.analysis.model.WeightRecord;

class UniformityAnalyzerTest {
    private final UniformityAnalyzer analyzer = new UniformityAnalyzer();

    @Test
    void returnsLowRiskWhenUniformityMeetsThreshold() {
        AnalysisResult result = analyzer.analyze(
                request(),
                batch(),
                Arrays.asList(weight("2026-05-20", 50, "82"), weight("2026-05-21", 51, "83")),
                Collections.singletonList(standard()));

        assertEquals(RiskLevel.LOW, result.getRiskLevel());
        assertTrue(result.getSummary().contains("Below-threshold days: 0"));
    }

    @Test
    void returnsHighRiskWhenUniformityIsConsecutivelyLow() {
        AnalysisResult result = analyzer.analyze(
                request(),
                batch(),
                Arrays.asList(weight("2026-05-20", 50, "76"), weight("2026-05-21", 51, "77")),
                Collections.singletonList(standard()));

        assertEquals(RiskLevel.HIGH, result.getRiskLevel());
        assertTrue(result.getReasons().get(0).contains("below threshold"));
    }

    @Test
    void returnsUnknownRiskWhenNoStandardMatches() {
        AnalysisResult result = analyzer.analyze(
                request(),
                batch(),
                Collections.singletonList(weight("2026-05-20", 50, "82")),
                Collections.emptyList());

        assertEquals(RiskLevel.UNKNOWN, result.getRiskLevel());
        assertTrue(result.getSummary().contains("missing-standard days: 1"));
    }

    private static AnalysisRequest request() {
        return new AnalysisRequest(
                "REQ-002",
                RequestSource.MANUAL_TEST,
                "ou_test",
                "BATCH-001",
                AnalysisType.UNIFORMITY,
                LocalDate.parse("2026-05-20"),
                LocalDate.parse("2026-05-21"),
                "Analyze uniformity");
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

    private static WeightRecord weight(String measuredDate, int ageDays, String uniformityPercent) {
        return new WeightRecord(
                "BATCH-001",
                LocalDate.parse(measuredDate),
                ageDays,
                new BigDecimal("1.42"),
                new BigDecimal(uniformityPercent),
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
