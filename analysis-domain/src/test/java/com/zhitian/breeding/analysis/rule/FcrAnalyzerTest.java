package com.zhitian.breeding.analysis.rule;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
import com.zhitian.breeding.analysis.model.FcrRecord;
import com.zhitian.breeding.analysis.model.FcrStandard;
import com.zhitian.breeding.analysis.model.RequestSource;
import com.zhitian.breeding.analysis.model.RiskLevel;

class FcrAnalyzerTest {
    private final FcrAnalyzer analyzer = new FcrAnalyzer();

    @Test
    void returnsLowRiskWhenFcrMeetsStandard() {
        AnalysisResult result = analyzer.analyze(
                request(),
                batch(),
                Arrays.asList(fcr("2026-05-20", 50, "120", "80"), fcr("2026-05-21", 51, "121", "80")),
                Collections.singletonList(standard()));

        assertEquals(RiskLevel.LOW, result.getRiskLevel());
        assertTrue(result.getSummary().contains("Above-threshold days: 0"));
    }

    @Test
    void returnsHighRiskWhenFcrIsConsecutivelyHigh() {
        AnalysisResult result = analyzer.analyze(
                request(),
                batch(),
                Arrays.asList(fcr("2026-05-20", 50, "150", "80"), fcr("2026-05-21", 51, "152", "80")),
                Collections.singletonList(standard()));

        assertEquals(RiskLevel.HIGH, result.getRiskLevel());
        assertTrue(result.getReasons().get(0).contains("above threshold"));
    }

    @Test
    void returnsUnknownRiskWhenNoStandardMatches() {
        AnalysisResult result = analyzer.analyze(
                request(),
                batch(),
                Collections.singletonList(fcr("2026-05-20", 50, "120", "80")),
                Collections.emptyList());

        assertEquals(RiskLevel.UNKNOWN, result.getRiskLevel());
        assertTrue(result.getSummary().contains("missing-standard days: 1"));
    }

    @Test
    void rejectsZeroWeightGain() {
        assertThrows(IllegalArgumentException.class, () -> fcr("2026-05-20", 50, "120", "0"));
    }

    private static AnalysisRequest request() {
        return new AnalysisRequest(
                "REQ-003",
                RequestSource.MANUAL_TEST,
                "ou_test",
                "BATCH-001",
                AnalysisType.FEED_CONVERSION_RATIO,
                LocalDate.parse("2026-05-20"),
                LocalDate.parse("2026-05-21"),
                "Analyze FCR");
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

    private static FcrRecord fcr(String recordDate, int ageDays, String feedConsumedKg, String weightGainKg) {
        return new FcrRecord(
                "BATCH-001",
                LocalDate.parse(recordDate),
                ageDays,
                new BigDecimal(feedConsumedKg),
                new BigDecimal(weightGainKg));
    }

    private static FcrStandard standard() {
        return new FcrStandard(
                "Datu2",
                "Mixed",
                31,
                60,
                new BigDecimal("1.70"));
    }
}
