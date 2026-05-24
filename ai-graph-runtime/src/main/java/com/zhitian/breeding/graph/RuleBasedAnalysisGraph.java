package com.zhitian.breeding.graph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.zhitian.breeding.analysis.model.AnalysisRequest;
import com.zhitian.breeding.analysis.model.AnalysisResult;
import com.zhitian.breeding.analysis.model.BreedingBatch;
import com.zhitian.breeding.analysis.model.BreedingStandard;
import com.zhitian.breeding.analysis.model.FcrRecord;
import com.zhitian.breeding.analysis.model.FcrStandard;
import com.zhitian.breeding.analysis.model.RiskLevel;
import com.zhitian.breeding.analysis.model.WeightRecord;
import com.zhitian.breeding.analysis.rule.FcrAnalyzer;
import com.zhitian.breeding.analysis.rule.UniformityAnalyzer;
import com.zhitian.breeding.analysis.rule.WeightTrendAnalyzer;
import com.zhitian.breeding.lark.base.AiBaseWriteClient;
import com.zhitian.breeding.lark.base.BreedingBaseClient;
import com.zhitian.breeding.lark.base.FcrBaseClient;

public final class RuleBasedAnalysisGraph implements AnalysisGraph {
    private final BreedingBaseClient breedingBaseClient;
    private final FcrBaseClient fcrBaseClient;
    private final AiBaseWriteClient writeClient;
    private final WeightTrendAnalyzer weightTrendAnalyzer;
    private final UniformityAnalyzer uniformityAnalyzer;
    private final FcrAnalyzer fcrAnalyzer;

    public RuleBasedAnalysisGraph(
            BreedingBaseClient breedingBaseClient,
            FcrBaseClient fcrBaseClient,
            AiBaseWriteClient writeClient) {
        this(
                breedingBaseClient,
                fcrBaseClient,
                writeClient,
                new WeightTrendAnalyzer(),
                new UniformityAnalyzer(),
                new FcrAnalyzer());
    }

    public RuleBasedAnalysisGraph(
            BreedingBaseClient breedingBaseClient,
            FcrBaseClient fcrBaseClient,
            AiBaseWriteClient writeClient,
            WeightTrendAnalyzer weightTrendAnalyzer,
            UniformityAnalyzer uniformityAnalyzer,
            FcrAnalyzer fcrAnalyzer) {
        this.breedingBaseClient = Objects.requireNonNull(breedingBaseClient, "breedingBaseClient");
        this.fcrBaseClient = Objects.requireNonNull(fcrBaseClient, "fcrBaseClient");
        this.writeClient = Objects.requireNonNull(writeClient, "writeClient");
        this.weightTrendAnalyzer = Objects.requireNonNull(weightTrendAnalyzer, "weightTrendAnalyzer");
        this.uniformityAnalyzer = Objects.requireNonNull(uniformityAnalyzer, "uniformityAnalyzer");
        this.fcrAnalyzer = Objects.requireNonNull(fcrAnalyzer, "fcrAnalyzer");
    }

    @Override
    public AnalysisResult run(AnalysisRequest request) {
        Objects.requireNonNull(request, "request");
        writeClient.createAnalysisRequest(request);
        AnalysisResult result = dispatch(request);
        writeClient.saveAnalysisResult(result);
        return result;
    }

    private AnalysisResult dispatch(AnalysisRequest request) {
        Optional<BreedingBatch> batch = breedingBaseClient.findBatchById(request.getBatchId());
        if (!batch.isPresent()) {
            return new AnalysisResult(
                    request.getRequestId(),
                    RiskLevel.UNKNOWN,
                    "Batch " + request.getBatchId() + " was not found.",
                    Collections.singletonList("The requested batch does not exist in the current Base dataset."),
                    Collections.singletonList("Check the batch ID or synchronize the batch table before retrying."));
        }

        switch (request.getAnalysisType()) {
            case WEIGHT_TREND:
                return analyzeWeightTrend(request, batch.get());
            case UNIFORMITY:
                return analyzeUniformity(request, batch.get());
            case FEED_CONVERSION_RATIO:
                return analyzeFcr(request, batch.get());
            default:
                return new AnalysisResult(
                        request.getRequestId(),
                        RiskLevel.UNKNOWN,
                        "Analysis type " + request.getAnalysisType() + " is not supported by the rule-based graph yet.",
                        Collections.singletonList("The current graph supports weight trend, uniformity, and FCR analysis."),
                        Collections.singletonList("Route this request to a later LLM/RAG graph node when available."));
        }
    }

    private AnalysisResult analyzeWeightTrend(AnalysisRequest request, BreedingBatch batch) {
        List<WeightRecord> records = breedingBaseClient.listWeightRecords(
                request.getBatchId(),
                request.getStartDate(),
                request.getEndDate());
        return weightTrendAnalyzer.analyze(request, batch, records, collectWeightStandards(batch, records));
    }

    private AnalysisResult analyzeUniformity(AnalysisRequest request, BreedingBatch batch) {
        List<WeightRecord> records = breedingBaseClient.listWeightRecords(
                request.getBatchId(),
                request.getStartDate(),
                request.getEndDate());
        return uniformityAnalyzer.analyze(request, batch, records, collectWeightStandards(batch, records));
    }

    private AnalysisResult analyzeFcr(AnalysisRequest request, BreedingBatch batch) {
        List<FcrRecord> records = fcrBaseClient.listFcrRecords(
                request.getBatchId(),
                request.getStartDate(),
                request.getEndDate());
        return fcrAnalyzer.analyze(request, batch, records, collectFcrStandards(batch, records));
    }

    private List<BreedingStandard> collectWeightStandards(BreedingBatch batch, List<WeightRecord> records) {
        List<BreedingStandard> standards = new ArrayList<>();
        for (WeightRecord record : records) {
            breedingBaseClient
                    .findStandard(batch.getBreedName(), batch.getFeedingMode(), record.getAgeDays())
                    .ifPresent(standards::add);
        }
        return standards;
    }

    private List<FcrStandard> collectFcrStandards(BreedingBatch batch, List<FcrRecord> records) {
        List<FcrStandard> standards = new ArrayList<>();
        for (FcrRecord record : records) {
            fcrBaseClient
                    .findFcrStandard(batch.getBreedName(), batch.getFeedingMode(), record.getAgeDays())
                    .ifPresent(standards::add);
        }
        return standards;
    }
}
