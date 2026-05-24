package com.wens.breeding.analysis.model;

import java.math.BigDecimal;
import java.util.Objects;

public final class FcrStandard {
    private final String breedName;
    private final String feedingMode;
    private final int startAgeDays;
    private final int endAgeDays;
    private final BigDecimal maxFcr;

    public FcrStandard(
            String breedName,
            String feedingMode,
            int startAgeDays,
            int endAgeDays,
            BigDecimal maxFcr) {
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
        this.maxFcr = Objects.requireNonNull(maxFcr, "maxFcr");
        if (maxFcr.signum() <= 0) {
            throw new IllegalArgumentException("maxFcr must be positive");
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

    public BigDecimal getMaxFcr() {
        return maxFcr;
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value;
    }
}
