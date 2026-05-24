package com.wens.breeding.app.baseapp;

import java.time.LocalDate;
import java.util.Locale;

import com.wens.breeding.analysis.model.AnalysisRequest;
import com.wens.breeding.analysis.model.AnalysisType;
import com.wens.breeding.analysis.model.RequestSource;

public final class BaseAppAnalysisRequest {
    private String requestId;
    private String requesterOpenId;
    private String batchId;
    private String analysisType;
    private String startDate;
    private String endDate;
    private String rawQuestion;

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getRequesterOpenId() {
        return requesterOpenId;
    }

    public void setRequesterOpenId(String requesterOpenId) {
        this.requesterOpenId = requesterOpenId;
    }

    public String getBatchId() {
        return batchId;
    }

    public void setBatchId(String batchId) {
        this.batchId = batchId;
    }

    public String getAnalysisType() {
        return analysisType;
    }

    public void setAnalysisType(String analysisType) {
        this.analysisType = analysisType;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getRawQuestion() {
        return rawQuestion;
    }

    public void setRawQuestion(String rawQuestion) {
        this.rawQuestion = rawQuestion;
    }

    AnalysisRequest toAnalysisRequest(String resolvedRequestId) {
        return new AnalysisRequest(
                requireText(resolvedRequestId, "requestId"),
                RequestSource.LARK_BASE_APP,
                requireText(requesterOpenId, "requesterOpenId"),
                requireText(batchId, "batchId"),
                parseAnalysisType(analysisType),
                parseDate(startDate, "startDate"),
                parseDate(endDate, "endDate"),
                rawQuestion);
    }

    private static AnalysisType parseAnalysisType(String value) {
        String normalized = requireText(value, "analysisType")
                .trim()
                .toUpperCase(Locale.ROOT);
        try {
            return AnalysisType.valueOf(normalized);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("analysisType is not supported: " + value, exception);
        }
    }

    private static LocalDate parseDate(String value, String fieldName) {
        try {
            return LocalDate.parse(requireText(value, fieldName));
        } catch (RuntimeException exception) {
            throw new IllegalArgumentException(fieldName + " must use ISO date format yyyy-MM-dd", exception);
        }
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value;
    }
}
