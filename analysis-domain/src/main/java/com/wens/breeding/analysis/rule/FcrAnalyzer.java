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
import com.wens.breeding.analysis.model.FcrRecord;
import com.wens.breeding.analysis.model.FcrStandard;
import com.wens.breeding.analysis.model.RiskLevel;

public final class FcrAnalyzer {
    public AnalysisResult analyze(
            AnalysisRequest request,
            BreedingBatch batch,
            List<FcrRecord> records,
            List<FcrStandard> standards) {
        Objects.requireNonNull(request, "request");
        Objects.requireNonNull(batch, "batch");
        List<FcrRecord> sortedRecords = sorted(records);
        if (sortedRecords.isEmpty()) {
            return new AnalysisResult(
                    request.getRequestId(),
                    RiskLevel.UNKNOWN,
                    "No FCR records were found for the requested period.",
                    java.util.Collections.singletonList("The selected date range has no feed conversion data."),
                    java.util.Collections.singletonList("Confirm feed and weight-gain data sync before reviewing FCR."));
        }

        int highCount = 0;
        int missingStandardCount = 0;
        int currentHighStreak = 0;
        int maxHighStreak = 0;
        List<String> reasons = new ArrayList<>();

        for (FcrRecord record : sortedRecords) {
            Optional<FcrStandard> standard = findStandard(batch, record, standards);
            if (!standard.isPresent()) {
                missingStandardCount++;
                continue;
            }

            BigDecimal actualFcr = record.calculateFcr();
            FcrStandard matchedStandard = standard.get();
            if (actualFcr.compareTo(matchedStandard.getMaxFcr()) > 0) {
                highCount++;
                currentHighStreak++;
                maxHighStreak = Math.max(maxHighStreak, currentHighStreak);
                reasons.add(formatFcrReason(record, actualFcr, matchedStandard));
            } else {
                currentHighStreak = 0;
            }
        }

        RiskLevel riskLevel = decideRiskLevel(highCount, maxHighStreak, missingStandardCount, sortedRecords.size());
        String summary = buildSummary(batch, sortedRecords, riskLevel, highCount, missingStandardCount);
        List<String> suggestions = buildSuggestions(riskLevel, highCount, missingStandardCount);

        if (reasons.isEmpty()) {
            reasons.add("All matched FCR records are within the configured threshold.");
        }

        return new AnalysisResult(request.getRequestId(), riskLevel, summary, reasons, suggestions);
    }

    private static Optional<FcrStandard> findStandard(
            BreedingBatch batch,
            FcrRecord record,
            List<FcrStandard> standards) {
        return safeList(standards).stream()
                .filter(standard -> standard.matches(batch.getBreedName(), batch.getFeedingMode(), record.getAgeDays()))
                .findFirst();
    }

    private static RiskLevel decideRiskLevel(
            int highCount,
            int maxHighStreak,
            int missingStandardCount,
            int totalCount) {
        if (totalCount == missingStandardCount) {
            return RiskLevel.UNKNOWN;
        }
        if (maxHighStreak >= 2 || highCount >= 3) {
            return RiskLevel.HIGH;
        }
        if (highCount > 0 || missingStandardCount > 0) {
            return RiskLevel.MEDIUM;
        }
        return RiskLevel.LOW;
    }

    private static String buildSummary(
            BreedingBatch batch,
            List<FcrRecord> sortedRecords,
            RiskLevel riskLevel,
            int highCount,
            int missingStandardCount) {
        LocalDate startDate = sortedRecords.get(0).getRecordDate();
        LocalDate endDate = sortedRecords.get(sortedRecords.size() - 1).getRecordDate();
        return "Batch " + batch.getBatchId()
                + " FCR from " + startDate
                + " to " + endDate
                + " is " + riskLevel
                + ". Above-threshold days: " + highCount
                + ", missing-standard days: " + missingStandardCount
                + ".";
    }

    private static List<String> buildSuggestions(
            RiskLevel riskLevel,
            int highCount,
            int missingStandardCount) {
        List<String> suggestions = new ArrayList<>();
        if (highCount > 0) {
            suggestions.add("Review feed waste, mortality changes, weighing accuracy, and feed formula execution.");
        }
        if (missingStandardCount > 0) {
            suggestions.add("Complete FCR standards for unmatched age ranges before final review.");
        }
        if (riskLevel == RiskLevel.LOW) {
            suggestions.add("Keep current feed management and continue monitoring FCR trend.");
        }
        return suggestions;
    }

    private static String formatFcrReason(FcrRecord record, BigDecimal actualFcr, FcrStandard standard) {
        return "On " + record.getRecordDate()
                + ", FCR " + actualFcr
                + " is above threshold "
                + standard.getMaxFcr()
                + ".";
    }

    private static List<FcrRecord> sorted(List<FcrRecord> records) {
        List<FcrRecord> copy = new ArrayList<>(safeList(records));
        copy.sort(Comparator.comparing(FcrRecord::getRecordDate));
        return copy;
    }

    private static <T> List<T> safeList(List<T> values) {
        return values == null ? java.util.Collections.emptyList() : values;
    }
}
