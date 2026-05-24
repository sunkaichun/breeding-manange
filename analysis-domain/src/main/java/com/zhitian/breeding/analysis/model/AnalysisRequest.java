package com.zhitian.breeding.analysis.model;

import java.time.LocalDate;
import java.util.Objects;

public final class AnalysisRequest {
    private final String requestId;
    private final RequestSource source;
    private final String requesterOpenId;
    private final String batchId;
    private final AnalysisType analysisType;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final String rawQuestion;

    public AnalysisRequest(
            String requestId,
            RequestSource source,
            String requesterOpenId,
            String batchId,
            AnalysisType analysisType,
            LocalDate startDate,
            LocalDate endDate,
            String rawQuestion) {
        this.requestId = requireText(requestId, "requestId");
        this.source = Objects.requireNonNull(source, "source");
        this.requesterOpenId = requireText(requesterOpenId, "requesterOpenId");
        this.batchId = requireText(batchId, "batchId");
        this.analysisType = Objects.requireNonNull(analysisType, "analysisType");
        this.startDate = Objects.requireNonNull(startDate, "startDate");
        this.endDate = Objects.requireNonNull(endDate, "endDate");
        this.rawQuestion = rawQuestion == null ? "" : rawQuestion;
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("endDate must not be before startDate");
        }
    }

    public String getRequestId() {
        return requestId;
    }

    public RequestSource getSource() {
        return source;
    }

    public String getRequesterOpenId() {
        return requesterOpenId;
    }

    public String getBatchId() {
        return batchId;
    }

    public AnalysisType getAnalysisType() {
        return analysisType;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public String getRawQuestion() {
        return rawQuestion;
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value;
    }
}
