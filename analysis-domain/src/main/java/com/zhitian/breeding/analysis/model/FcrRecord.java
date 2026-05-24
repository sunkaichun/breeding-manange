package com.zhitian.breeding.analysis.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Objects;

public final class FcrRecord {
    private final String batchId;
    private final LocalDate recordDate;
    private final int ageDays;
    private final BigDecimal feedConsumedKg;
    private final BigDecimal weightGainKg;

    public FcrRecord(
            String batchId,
            LocalDate recordDate,
            int ageDays,
            BigDecimal feedConsumedKg,
            BigDecimal weightGainKg) {
        if (ageDays < 0) {
            throw new IllegalArgumentException("ageDays must be non-negative");
        }
        this.batchId = requireText(batchId, "batchId");
        this.recordDate = Objects.requireNonNull(recordDate, "recordDate");
        this.ageDays = ageDays;
        this.feedConsumedKg = requireNonNegative(feedConsumedKg, "feedConsumedKg");
        this.weightGainKg = requirePositive(weightGainKg, "weightGainKg");
    }

    public BigDecimal calculateFcr() {
        return feedConsumedKg.divide(weightGainKg, 4, RoundingMode.HALF_UP);
    }

    public String getBatchId() {
        return batchId;
    }

    public LocalDate getRecordDate() {
        return recordDate;
    }

    public int getAgeDays() {
        return ageDays;
    }

    public BigDecimal getFeedConsumedKg() {
        return feedConsumedKg;
    }

    public BigDecimal getWeightGainKg() {
        return weightGainKg;
    }

    private static BigDecimal requireNonNegative(BigDecimal value, String fieldName) {
        BigDecimal nonNullValue = Objects.requireNonNull(value, fieldName);
        if (nonNullValue.signum() < 0) {
            throw new IllegalArgumentException(fieldName + " must be non-negative");
        }
        return nonNullValue;
    }

    private static BigDecimal requirePositive(BigDecimal value, String fieldName) {
        BigDecimal nonNullValue = Objects.requireNonNull(value, fieldName);
        if (nonNullValue.signum() <= 0) {
            throw new IllegalArgumentException(fieldName + " must be positive");
        }
        return nonNullValue;
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value;
    }
}
