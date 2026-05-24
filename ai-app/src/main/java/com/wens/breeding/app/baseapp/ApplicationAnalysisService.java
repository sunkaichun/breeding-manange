package com.wens.breeding.app.baseapp;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import com.wens.breeding.analysis.model.AnalysisRequest;
import com.wens.breeding.analysis.model.AnalysisResult;
import com.wens.breeding.analysis.model.AnalysisType;
import com.wens.breeding.analysis.model.BreedingBatch;
import com.wens.breeding.analysis.model.BreedingStandard;
import com.wens.breeding.analysis.model.WeightRecord;
import com.wens.breeding.graph.AnalysisGraph;
import com.wens.breeding.lark.base.AiBaseWriteClient;
import com.wens.breeding.lark.base.BreedingBaseClient;
import com.wens.breeding.lark.base.VisualizationDataRecord;
import com.wens.breeding.visualization.WeightTrendVisualizationGenerator;

import org.springframework.stereotype.Service;

@Service
public class ApplicationAnalysisService {
    private final AnalysisGraph analysisGraph;
    private final BreedingBaseClient breedingBaseClient;
    private final AiBaseWriteClient writeClient;
    private final WeightTrendVisualizationGenerator visualizationGenerator;

    public ApplicationAnalysisService(
            AnalysisGraph analysisGraph,
            BreedingBaseClient breedingBaseClient,
            AiBaseWriteClient writeClient,
            WeightTrendVisualizationGenerator visualizationGenerator) {
        this.analysisGraph = Objects.requireNonNull(analysisGraph, "analysisGraph");
        this.breedingBaseClient = Objects.requireNonNull(breedingBaseClient, "breedingBaseClient");
        this.writeClient = Objects.requireNonNull(writeClient, "writeClient");
        this.visualizationGenerator = Objects.requireNonNull(visualizationGenerator, "visualizationGenerator");
    }

    public BaseAppAnalysisResponse submit(BaseAppAnalysisRequest request) {
        Objects.requireNonNull(request, "request");
        AnalysisRequest analysisRequest = request.toAnalysisRequest(resolveRequestId(request.getRequestId()));
        AnalysisResult result = analysisGraph.run(analysisRequest);
        List<String> visualizationRecordIds = writeVisualizationData(analysisRequest);
        return BaseAppAnalysisResponse.from(result, visualizationRecordIds);
    }

    private List<String> writeVisualizationData(AnalysisRequest request) {
        if (request.getAnalysisType() != AnalysisType.WEIGHT_TREND) {
            return java.util.Collections.emptyList();
        }

        Optional<BreedingBatch> batch = breedingBaseClient.findBatchById(request.getBatchId());
        if (!batch.isPresent()) {
            return java.util.Collections.emptyList();
        }

        List<WeightRecord> weightRecords = breedingBaseClient.listWeightRecords(
                request.getBatchId(),
                request.getStartDate(),
                request.getEndDate());
        List<BreedingStandard> standards = collectStandards(batch.get(), weightRecords);
        List<String> recordIds = new ArrayList<>();
        for (VisualizationDataRecord visualizationData : visualizationGenerator.generate(
                request,
                batch.get(),
                weightRecords,
                standards)) {
            recordIds.add(writeClient.saveVisualizationData(visualizationData));
        }
        return recordIds;
    }

    private List<BreedingStandard> collectStandards(BreedingBatch batch, List<WeightRecord> weightRecords) {
        List<BreedingStandard> standards = new ArrayList<>();
        for (WeightRecord record : weightRecords) {
            breedingBaseClient
                    .findStandard(batch.getBreedName(), batch.getFeedingMode(), record.getAgeDays())
                    .ifPresent(standards::add);
        }
        return standards;
    }

    private static String resolveRequestId(String requestId) {
        if (requestId != null && !requestId.trim().isEmpty()) {
            return requestId.trim();
        }
        return "BASEAPP-" + UUID.randomUUID();
    }
}
