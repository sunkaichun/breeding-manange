package com.wens.breeding.graph.execution.node;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.wens.breeding.analysis.model.AnalysisRequest;
import com.wens.breeding.analysis.model.AnalysisResult;
import com.wens.breeding.analysis.model.BreedingBatch;
import com.wens.breeding.analysis.model.BreedingStandard;
import com.wens.breeding.analysis.model.FcrRecord;
import com.wens.breeding.analysis.model.FcrStandard;
import com.wens.breeding.analysis.model.RiskLevel;
import com.wens.breeding.analysis.model.WeightRecord;
import com.wens.breeding.analysis.rule.FcrAnalyzer;
import com.wens.breeding.analysis.rule.UniformityAnalyzer;
import com.wens.breeding.analysis.rule.WeightTrendAnalyzer;
import com.wens.breeding.graph.execution.AiExecutionContext;
import com.wens.breeding.graph.execution.AiExecutionNode;
import com.wens.breeding.graph.execution.AiExecutionNodeResult;
import com.wens.breeding.lark.base.BreedingBaseClient;
import com.wens.breeding.lark.base.FcrBaseClient;

public final class RuleAnalysisNode implements AiExecutionNode {
    private final BreedingBaseClient breedingBaseClient;
    private final FcrBaseClient fcrBaseClient;
    private final WeightTrendAnalyzer weightTrendAnalyzer;
    private final UniformityAnalyzer uniformityAnalyzer;
    private final FcrAnalyzer fcrAnalyzer;

    public RuleAnalysisNode(
            BreedingBaseClient breedingBaseClient,
            FcrBaseClient fcrBaseClient,
            WeightTrendAnalyzer weightTrendAnalyzer,
            UniformityAnalyzer uniformityAnalyzer,
            FcrAnalyzer fcrAnalyzer) {
        this.breedingBaseClient = Objects.requireNonNull(breedingBaseClient, "breedingBaseClient");
        this.fcrBaseClient = Objects.requireNonNull(fcrBaseClient, "fcrBaseClient");
        this.weightTrendAnalyzer = Objects.requireNonNull(weightTrendAnalyzer, "weightTrendAnalyzer");
        this.uniformityAnalyzer = Objects.requireNonNull(uniformityAnalyzer, "uniformityAnalyzer");
        this.fcrAnalyzer = Objects.requireNonNull(fcrAnalyzer, "fcrAnalyzer");
    }

    @Override
    public String name() {
        return "rule-analysis";
    }

    @Override
    public AiExecutionNodeResult execute(AiExecutionContext context) {
        if (context.hasAnalysisResult()) {
            return AiExecutionNodeResult.skipped("Analysis result already exists");
        }
        if (!context.hasBatch()) {
            return AiExecutionNodeResult.skipped("Batch was not loaded");
        }

        AnalysisRequest request = context.getRequest();
        BreedingBatch batch = context.getBatch();
        AnalysisResult result;
        switch (request.getAnalysisType()) {
            case WEIGHT_TREND:
                result = analyzeWeightTrend(context, request, batch);
                break;
            case UNIFORMITY:
                result = analyzeUniformity(context, request, batch);
                break;
            case FEED_CONVERSION_RATIO:
                result = analyzeFcr(context, request, batch);
                break;
            default:
                result = unsupported(request);
                break;
        }
        context.setAnalysisResult(result);
        return AiExecutionNodeResult.completed("Rule analysis completed for " + request.getAnalysisType());
    }

    private AnalysisResult analyzeWeightTrend(AiExecutionContext context, AnalysisRequest request, BreedingBatch batch) {
        List<WeightRecord> records = loadWeightRecords(request);
        List<BreedingStandard> standards = collectWeightStandards(batch, records);
        context.setWeightRecords(records);
        context.setWeightStandards(standards);
        return weightTrendAnalyzer.analyze(request, batch, records, standards);
    }

    private AnalysisResult analyzeUniformity(AiExecutionContext context, AnalysisRequest request, BreedingBatch batch) {
        List<WeightRecord> records = loadWeightRecords(request);
        List<BreedingStandard> standards = collectWeightStandards(batch, records);
        context.setWeightRecords(records);
        context.setWeightStandards(standards);
        return uniformityAnalyzer.analyze(request, batch, records, standards);
    }

    private AnalysisResult analyzeFcr(AiExecutionContext context, AnalysisRequest request, BreedingBatch batch) {
        List<FcrRecord> records = fcrBaseClient.listFcrRecords(
                request.getBatchId(),
                request.getStartDate(),
                request.getEndDate());
        List<FcrStandard> standards = collectFcrStandards(batch, records);
        context.setFcrRecords(records);
        context.setFcrStandards(standards);
        return fcrAnalyzer.analyze(request, batch, records, standards);
    }

    private List<WeightRecord> loadWeightRecords(AnalysisRequest request) {
        return breedingBaseClient.listWeightRecords(
                request.getBatchId(),
                request.getStartDate(),
                request.getEndDate());
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

    private static AnalysisResult unsupported(AnalysisRequest request) {
        return new AnalysisResult(
                request.getRequestId(),
                RiskLevel.UNKNOWN,
                "Analysis type " + request.getAnalysisType() + " is not supported by the rule-based graph yet.",
                Collections.singletonList("The current graph supports weight trend, uniformity, and FCR analysis."),
                Collections.singletonList("Route this request to a later LLM/RAG graph node when available."));
    }
}
