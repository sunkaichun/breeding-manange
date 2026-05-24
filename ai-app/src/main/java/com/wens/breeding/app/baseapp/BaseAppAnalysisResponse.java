package com.wens.breeding.app.baseapp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.wens.breeding.analysis.model.AnalysisResult;
import com.wens.breeding.analysis.model.RiskLevel;

public final class BaseAppAnalysisResponse {
    private final String requestId;
    private final String status;
    private final RiskLevel riskLevel;
    private final String summary;
    private final List<String> reasons;
    private final List<String> suggestions;
    private final List<String> visualizationRecordIds;

    public BaseAppAnalysisResponse(
            String requestId,
            String status,
            RiskLevel riskLevel,
            String summary,
            List<String> reasons,
            List<String> suggestions,
            List<String> visualizationRecordIds) {
        this.requestId = requestId;
        this.status = status;
        this.riskLevel = riskLevel;
        this.summary = summary;
        this.reasons = immutableCopy(reasons);
        this.suggestions = immutableCopy(suggestions);
        this.visualizationRecordIds = immutableCopy(visualizationRecordIds);
    }

    public static BaseAppAnalysisResponse from(AnalysisResult result, List<String> visualizationRecordIds) {
        return new BaseAppAnalysisResponse(
                result.getRequestId(),
                "COMPLETED",
                result.getRiskLevel(),
                result.getSummary(),
                result.getReasons(),
                result.getSuggestions(),
                visualizationRecordIds);
    }

    public String getRequestId() {
        return requestId;
    }

    public String getStatus() {
        return status;
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

    public List<String> getVisualizationRecordIds() {
        return visualizationRecordIds;
    }

    private static List<String> immutableCopy(List<String> values) {
        if (values == null || values.isEmpty()) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(new ArrayList<>(values));
    }
}
