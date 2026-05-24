package com.zhitian.breeding.graph;

import com.zhitian.breeding.analysis.model.AnalysisRequest;
import com.zhitian.breeding.analysis.model.AnalysisResult;

public interface AnalysisGraph {
    AnalysisResult run(AnalysisRequest request);
}
