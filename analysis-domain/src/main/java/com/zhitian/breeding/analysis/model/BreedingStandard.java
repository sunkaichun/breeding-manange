package com.zhitian.breeding.analysis.model;

import java.math.BigDecimal;
import java.util.Objects;

public final class BreedingStandard {
    private final String breedName;
    private final String feedingMode;
    private final int startAgeDays;
    private final int endAgeDays;
    private final BigDecimal minWeightKg;
    private final BigDecimal maxWeightKg;
    private final BigDecimal minUniformityPercent;

    public BreedingStandard(
            String breedName,
            String feedingMode,
            int startAgeDays,
            int endAgeDays,
            BigDecimal minWeightKg,
            BigDecimal maxWeightKg,
            BigDecimal minUniformityPercent) {
        if (startAgeDays < 0) {
            throw new IllegalArgumentException("startAgeDays must be non-negative");
        }
        if (endAgeDays < startAgeDays) {
            throw new IllegalArgumentException("endAgeDays must not be before startAgeDays");
        }
        this.breedName = requireText(breedName, "breedName");
        this.feedingMode = requireText(feedingMode, "feedingMode");
        this.startAgeDays = startAgeDays;
        this.endAgeDays = endAgeDays;
        this.minWeightKg = Objects.requireNonNull(minWeightKg, "minWeightKg");
        this.maxWeightKg = Objects.requireNonNull(maxWeightKg, "maxWeightKg");
        this.minUniformityPercent = Objects.requireNonNull(minUniformityPercent, "minUniformityPercent");
        if (maxWeightKg.compareTo(minWeightKg) < 0) {
            throw new IllegalArgumentException("maxWeightKg must not be less than minWeightKg");
        }
    }

    public boolean matches(String breedName, String feedingMode, int ageDays) {
        return this.breedName.equals(breedName)
                && this.feedingMode.equals(feedingMode)
                && ageDays >= startAgeDays
                && ageDays <= endAgeDays;
    }

    public String getBreedName() {
        return breedName;
    }

    public String getFeedingMode() {
        return feedingMode;
    }

    public int getStartAgeDays() {
        return startAgeDays;
    }

    public int getEndAgeDays() {
        return endAgeDays;
    }

    public BigDecimal getMinWeightKg() {
        return minWeightKg;
    }

    public BigDecimal getMaxWeightKg() {
        return maxWeightKg;
    }

    public BigDecimal getMinUniformityPercent() {
        return minUniformityPercent;
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value;
    }
}
