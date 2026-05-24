package com.wens.breeding.graph.execution.node;

import java.util.Collections;
import java.util.Objects;
import java.util.Optional;

import com.wens.breeding.analysis.model.AnalysisRequest;
import com.wens.breeding.analysis.model.AnalysisResult;
import com.wens.breeding.analysis.model.BreedingBatch;
import com.wens.breeding.analysis.model.RiskLevel;
import com.wens.breeding.graph.execution.AiExecutionContext;
import com.wens.breeding.graph.execution.AiExecutionNode;
import com.wens.breeding.graph.execution.AiExecutionNodeResult;
import com.wens.breeding.lark.base.BreedingBaseClient;

public final class LoadBatchNode implements AiExecutionNode {
    private final BreedingBaseClient breedingBaseClient;

    public LoadBatchNode(BreedingBaseClient breedingBaseClient) {
        this.breedingBaseClient = Objects.requireNonNull(breedingBaseClient, "breedingBaseClient");
    }

    @Override
    public String name() {
        return "load-batch";
    }

    @Override
    public AiExecutionNodeResult execute(AiExecutionContext context) {
        AnalysisRequest request = context.getRequest();
        Optional<BreedingBatch> batch = breedingBaseClient.findBatchById(request.getBatchId());
        if (!batch.isPresent()) {
            context.setAnalysisResult(new AnalysisResult(
                    request.getRequestId(),
                    RiskLevel.UNKNOWN,
                    "Batch " + request.getBatchId() + " was not found.",
                    Collections.singletonList("The requested batch does not exist in the current Base dataset."),
                    Collections.singletonList("Check the batch ID or synchronize the batch table before retrying.")));
            return AiExecutionNodeResult.completed("Batch is missing; fallback result prepared");
        }

        context.setBatch(batch.get());
        return AiExecutionNodeResult.completed("Batch loaded: " + batch.get().getBatchId());
    }
}
