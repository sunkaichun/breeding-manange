package com.zhitian.breeding.analysis.model;

import java.time.LocalDate;
import java.util.Objects;

public final class BreedingBatch {
    private final String batchId;
    private final String organizationName;
    private final String breedName;
    private final String feedingMode;
    private final LocalDate entryDate;
    private final LocalDate plannedMarketDate;
    private final String responsibleOpenId;
    private final String responsibleName;

    public BreedingBatch(
            String batchId,
            String organizationName,
            String breedName,
            String feedingMode,
            LocalDate entryDate,
            LocalDate plannedMarketDate,
            String responsibleOpenId,
            String responsibleName) {
        this.batchId = requireText(batchId, "batchId");
        this.organizationName = requireText(organizationName, "organizationName");
        this.breedName = requireText(breedName, "breedName");
        this.feedingMode = requireText(feedingMode, "feedingMode");
        this.entryDate = Objects.requireNonNull(entryDate, "entryDate");
        this.plannedMarketDate = plannedMarketDate;
        this.responsibleOpenId = responsibleOpenId == null ? "" : responsibleOpenId;
        this.responsibleName = responsibleName == null ? "" : responsibleName;
    }

    public String getBatchId() {
        return batchId;
    }

    public String getOrganizationName() {
        return organizationName;
    }

    public String getBreedName() {
        return breedName;
    }

    public String getFeedingMode() {
        return feedingMode;
    }

    public LocalDate getEntryDate() {
        return entryDate;
    }

    public LocalDate getPlannedMarketDate() {
        return plannedMarketDate;
    }

    public String getResponsibleOpenId() {
        return responsibleOpenId;
    }

    public String getResponsibleName() {
        return responsibleName;
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value;
    }
}
