package com.wens.breeding.analysis.rule;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.wens.breeding.analysis.model.AnalysisRequest;
import com.wens.breeding.analysis.model.AnalysisResult;
import com.wens.breeding.analysis.model.BreedingBatch;
import com.wens.breeding.analysis.model.BreedingStandard;
import com.wens.breeding.analysis.model.RiskLevel;
import com.wens.breeding.analysis.model.WeightRecord;

public final class WeightTrendAnalyzer {
    public AnalysisResult analyze(
            AnalysisRequest request,
            BreedingBatch batch,
            List<WeightRecord> records,
            List<BreedingStandard> standards) {
        Objects.requireNonNull(request, "request");
        Objects.requireNonNull(batch, "batch");
        List<WeightRecord> sortedRecords = sorted(records);
        if (sortedRecords.isEmpty()) {
            return new AnalysisResult(
                    request.getRequestId(),
                    RiskLevel.UNKNOWN,
                    "No weight records were found for the requested period.",
                    java.util.Collections.singletonList("The selected date range has no weight data."),
                    java.util.Collections.singletonList("Confirm weighing data sync before making management decisions."));
        }

        int belowCount = 0;
        int aboveCount = 0;
        int missingStandardCount = 0;
        int currentBelowStreak = 0;
        int maxBelowStreak = 0;
        List<String> reasons = new ArrayList<>();

        for (WeightRecord record : sortedRecords) {
            Optional<BreedingStandard> standard = findStandard(batch, record, standards);
            if (!standard.isPresent()) {
                missingStandardCount++;
                continue;
            }

            BreedingStandard matchedStandard = standard.get();
            int lowCompare = record.getAverageWeightKg().compareTo(matchedStandard.getMinWeightKg());
            int highCompare = record.getAverageWeightKg().compareTo(matchedStandard.getMaxWeightKg());
            if (lowCompare < 0) {
                belowCount++;
                currentBelowStreak++;
                maxBelowStreak = Math.max(maxBelowStreak, currentBelowStreak);
                reasons.add(formatWeightReason(record.getMeasuredDate(), "below", record.getAverageWeightKg(), matchedStandard));
            } else if (highCompare > 0) {
                aboveCount++;
                currentBelowStreak = 0;
                reasons.add(formatWeightReason(record.getMeasuredDate(), "above", record.getAverageWeightKg(), matchedStandard));
            } else {
                currentBelowStreak = 0;
            }
        }

        RiskLevel riskLevel = decideRiskLevel(belowCount, aboveCount, maxBelowStreak, missingStandardCount, sortedRecords.size());
        String summary = buildSummary(batch, sortedRecords, riskLevel, belowCount, aboveCount, missingStandardCount);
        List<String> suggestions = buildSuggestions(riskLevel, belowCount, aboveCount, missingStandardCount);

        if (reasons.isEmpty()) {
            reasons.add("All matched weight records are within the configured standard range.");
        }

        return new AnalysisResult(request.getRequestId(), riskLevel, summary, reasons, suggestions);
    }

    private static Optional<BreedingStandard> findStandard(
            BreedingBatch batch,
            WeightRecord record,
            List<BreedingStandard> standards) {
        return safeList(standards).stream()
                .filter(standard -> standard.matches(batch.getBreedName(), batch.getFeedingMode(), record.getAgeDays()))
                .findFirst();
    }

    private static RiskLevel decideRiskLevel(
            int belowCount,
            int aboveCount,
            int maxBelowStreak,
            int missingStandardCount,
            int totalCount) {
        if (totalCount == missingStandardCount) {
            return RiskLevel.UNKNOWN;
        }
        if (maxBelowStreak >= 2 || belowCount >= 3) {
            return RiskLevel.HIGH;
        }
        if (belowCount > 0 || aboveCount > 0 || missingStandardCount > 0) {
            return RiskLevel.MEDIUM;
        }
        return RiskLevel.LOW;
    }

    private static String buildSummary(
            BreedingBatch batch,
            List<WeightRecord> sortedRecords,
            RiskLevel riskLevel,
            int belowCount,
            int aboveCount,
            int missingStandardCount) {
        LocalDate startDate = sortedRecords.get(0).getMeasuredDate();
        LocalDate endDate = sortedRecords.get(sortedRecords.size() - 1).getMeasuredDate();
        return "Batch " + batch.getBatchId()
                + " weight trend from " + startDate
                + " to " + endDate
                + " is " + riskLevel
                + ". Below-standard days: " + belowCount
                + ", above-standard days: " + aboveCount
                + ", missing-standard days: " + missingStandardCount
                + ".";
    }

    private static List<String> buildSuggestions(
            RiskLevel riskLevel,
            int belowCount,
            int aboveCount,
            int missingStandardCount) {
        List<String> suggestions = new ArrayList<>();
        if (belowCount > 0) {
            suggestions.add("Review feed intake, health status, density, and temperature for below-standard weight days.");
        }
        if (aboveCount > 0) {
            suggestions.add("Check whether growth is ahead of plan and whether market readiness should be reviewed.");
        }
        if (missingStandardCount > 0) {
            suggestions.add("Complete breeding standard records for the unmatched age ranges before final review.");
        }
        if (riskLevel == RiskLevel.LOW) {
            suggestions.add("Keep the current feeding and monitoring plan.");
        }
        return suggestions;
    }

    private static String formatWeightReason(
            LocalDate measuredDate,
            String direction,
            BigDecimal actualWeight,
            BreedingStandard standard) {
        return "On " + measuredDate
                + ", average weight " + actualWeight
                + " kg is " + direction
                + " standard range " + standard.getMinWeightKg()
                + "-" + standard.getMaxWeightKg()
                + " kg.";
    }

    private static List<WeightRecord> sorted(List<WeightRecord> records) {
        List<WeightRecord> copy = new ArrayList<>(safeList(records));
        copy.sort(Comparator.comparing(WeightRecord::getMeasuredDate));
        return copy;
    }

    private static <T> List<T> safeList(List<T> values) {
        return values == null ? java.util.Collections.emptyList() : values;
    }
}
