package com.wens.breeding.graph.execution;

import com.wens.breeding.analysis.model.AnalysisRequest;

public interface AiExecutionEngine {
    AiExecutionResult execute(AnalysisRequest request);
}
