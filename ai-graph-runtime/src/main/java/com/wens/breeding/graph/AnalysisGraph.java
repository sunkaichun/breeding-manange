package com.wens.breeding.graph;

import com.wens.breeding.analysis.model.AnalysisRequest;
import com.wens.breeding.analysis.model.AnalysisResult;

public interface AnalysisGraph {
    AnalysisResult run(AnalysisRequest request);
}
