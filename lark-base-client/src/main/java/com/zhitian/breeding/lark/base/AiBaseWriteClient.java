package com.zhitian.breeding.lark.base;

import com.zhitian.breeding.analysis.model.AnalysisRequest;
import com.zhitian.breeding.analysis.model.AnalysisResult;

public interface AiBaseWriteClient {
    String createAnalysisRequest(AnalysisRequest request);

    String saveAnalysisResult(AnalysisResult result);

    String saveVisualizationData(VisualizationDataRecord visualizationData);
}
