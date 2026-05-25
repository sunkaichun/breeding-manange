package com.wens.breeding.analysis.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

public final class WeightRecord implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String batchId;
    private final LocalDate measuredDate;
    private final int ageDays;
    private final BigDecimal averageWeightKg;
    private final BigDecimal uniformityPercent;
    private final int stockCount;

    public WeightRecord(
            String batchId,
            LocalDate measuredDate,
            int ageDays,
            BigDecimal averageWeightKg,
            BigDecimal uniformityPercent,
            int stockCount) {
        if (ageDays < 0) {
            throw new IllegalArgumentException("ageDays must be non-negative");
        }
        if (stockCount < 0) {
            throw new IllegalArgumentException("stockCount must be non-negative");
        }
        this.batchId = requireText(batchId, "batchId");
        this.measuredDate = Objects.requireNonNull(measuredDate, "measuredDate");
        this.ageDays = ageDays;
        this.averageWeightKg = Objects.requireNonNull(averageWeightKg, "averageWeightKg");
        this.uniformityPercent = Objects.requireNonNull(uniformityPercent, "uniformityPercent");
        this.stockCount = stockCount;
    }

    public String getBatchId() {
        return batchId;
    }

    public LocalDate getMeasuredDate() {
        return measuredDate;
    }

    public int getAgeDays() {
        return ageDays;
    }

    public BigDecimal getAverageWeightKg() {
        return averageWeightKg;
    }

    public BigDecimal getUniformityPercent() {
        return uniformityPercent;
    }

    public int getStockCount() {
        return stockCount;
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value;
    }
}
