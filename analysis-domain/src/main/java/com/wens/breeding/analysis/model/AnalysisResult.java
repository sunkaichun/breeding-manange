package com.wens.breeding.analysis.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class AnalysisResult implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String requestId;
    private final RiskLevel riskLevel;
    private final String summary;
    private final List<String> reasons;
    private final List<String> suggestions;

    public AnalysisResult(
            String requestId,
            RiskLevel riskLevel,
            String summary,
            List<String> reasons,
            List<String> suggestions) {
        this.requestId = requireText(requestId, "requestId");
        this.riskLevel = riskLevel == null ? RiskLevel.UNKNOWN : riskLevel;
        this.summary = requireText(summary, "summary");
        this.reasons = immutableCopy(reasons);
        this.suggestions = immutableCopy(suggestions);
    }

    public String getRequestId() {
        return requestId;
    }

    public RiskLevel getRiskLevel() {
        return riskLevel;
    }

    public String getSummary() {
        return summary;
    }

    public List<String> getReasons() {
        return reasons;
    }

    public List<String> getSuggestions() {
        return suggestions;
    }

    private static List<String> immutableCopy(List<String> values) {
        if (values == null || values.isEmpty()) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(new ArrayList<>(values));
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value;
    }
}
