package com.wens.breeding.lark.base;

import com.wens.breeding.analysis.model.AnalysisRequest;
import com.wens.breeding.analysis.model.AnalysisResult;

public interface AiBaseWriteClient {
    String createAnalysisRequest(AnalysisRequest request);

    String saveAnalysisResult(AnalysisResult result);

    String saveVisualizationData(VisualizationDataRecord visualizationData);
}
