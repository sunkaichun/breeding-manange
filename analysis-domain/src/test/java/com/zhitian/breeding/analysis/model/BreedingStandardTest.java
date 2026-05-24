package com.zhitian.breeding.analysis.model;

import java.math.BigDecimal;

public final class BreedingStandardTest {
    private BreedingStandardTest() {
    }

    public static void main(String[] args) {
        BreedingStandard standard = new BreedingStandard(
                "Datu2",
                "Mixed",
                31,
                60,
                new BigDecimal("1.30"),
                new BigDecimal("1.58"),
                new BigDecimal("80"));

        if (!standard.matches("Datu2", "Mixed", 53)) {
            throw new AssertionError("Expected age 53 to match the configured standard range");
        }
        if (standard.matches("Datu2", "Mixed", 61)) {
            throw new AssertionError("Expected age 61 to be outside the configured standard range");
        }
    }
}
