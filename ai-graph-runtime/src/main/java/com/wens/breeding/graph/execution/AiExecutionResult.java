package com.wens.breeding.graph.execution;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.wens.breeding.analysis.model.AnalysisResult;

public final class AiExecutionResult {
    private final AiFramework framework;
    private final AnalysisResult analysisResult;
    private final List<AiExecutionTrace> traces;

    public AiExecutionResult(AiFramework framework, AnalysisResult analysisResult, List<AiExecutionTrace> traces) {
        this.framework = framework == null ? AiFramework.RULE_BASED : framework;
        if (analysisResult == null) {
            throw new IllegalArgumentException("analysisResult must not be null");
        }
        this.analysisResult = analysisResult;
        this.traces = traces == null
                ? Collections.emptyList()
                : Collections.unmodifiableList(new ArrayList<>(traces));
    }

    public AiFramework getFramework() {
        return framework;
    }

    public AnalysisResult getAnalysisResult() {
        return analysisResult;
    }

    public List<AiExecutionTrace> getTraces() {
        return traces;
    }
}
