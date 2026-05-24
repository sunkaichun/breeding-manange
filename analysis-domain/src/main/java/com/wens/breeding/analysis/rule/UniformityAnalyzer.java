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

public final class UniformityAnalyzer {
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
                    "No uniformity records were found for the requested period.",
                    java.util.Collections.singletonList("The selected date range has no weighing data."),
                    java.util.Collections.singletonList("Confirm weighing data sync before reviewing uniformity."));
        }

        int belowCount = 0;
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
            if (record.getUniformityPercent().compareTo(matchedStandard.getMinUniformityPercent()) < 0) {
                belowCount++;
                currentBelowStreak++;
                maxBelowStreak = Math.max(maxBelowStreak, currentBelowStreak);
                reasons.add(formatUniformityReason(record, matchedStandard));
            } else {
                currentBelowStreak = 0;
            }
        }

        RiskLevel riskLevel = decideRiskLevel(belowCount, maxBelowStreak, missingStandardCount, sortedRecords.size());
        String summary = buildSummary(batch, sortedRecords, riskLevel, belowCount, missingStandardCount);
        List<String> suggestions = buildSuggestions(riskLevel, belowCount, missingStandardCount);

        if (reasons.isEmpty()) {
            reasons.add("All matched uniformity records are above the configured threshold.");
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
            int maxBelowStreak,
            int missingStandardCount,
            int totalCount) {
        if (totalCount == missingStandardCount) {
            return RiskLevel.UNKNOWN;
        }
        if (maxBelowStreak >= 2 || belowCount >= 3) {
            return RiskLevel.HIGH;
        }
        if (belowCount > 0 || missingStandardCount > 0) {
            return RiskLevel.MEDIUM;
        }
        return RiskLevel.LOW;
    }

    private static String buildSummary(
            BreedingBatch batch,
            List<WeightRecord> sortedRecords,
            RiskLevel riskLevel,
            int belowCount,
            int missingStandardCount) {
        LocalDate startDate = sortedRecords.get(0).getMeasuredDate();
        LocalDate endDate = sortedRecords.get(sortedRecords.size() - 1).getMeasuredDate();
        return "Batch " + batch.getBatchId()
                + " uniformity from " + startDate
                + " to " + endDate
                + " is " + riskLevel
                + ". Below-threshold days: " + belowCount
                + ", missing-standard days: " + missingStandardCount
                + ".";
    }

    private static List<String> buildSuggestions(
            RiskLevel riskLevel,
            int belowCount,
            int missingStandardCount) {
        List<String> suggestions = new ArrayList<>();
        if (belowCount > 0) {
            suggestions.add("Review grading, weak-bird isolation, feeding space, and flock density.");
        }
        if (missingStandardCount > 0) {
            suggestions.add("Complete uniformity threshold standards for unmatched age ranges.");
        }
        if (riskLevel == RiskLevel.LOW) {
            suggestions.add("Keep current flock grading and feeding management.");
        }
        return suggestions;
    }

    private static String formatUniformityReason(WeightRecord record, BreedingStandard standard) {
        BigDecimal actualUniformity = record.getUniformityPercent();
        return "On " + record.getMeasuredDate()
                + ", uniformity " + actualUniformity
                + "% is below threshold "
                + standard.getMinUniformityPercent()
                + "%.";
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
