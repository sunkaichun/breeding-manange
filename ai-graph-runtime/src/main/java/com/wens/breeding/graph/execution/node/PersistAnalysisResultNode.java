package com.wens.breeding.graph.execution.node;

import java.util.Objects;

import com.wens.breeding.graph.execution.AiExecutionContext;
import com.wens.breeding.graph.execution.AiExecutionNode;
import com.wens.breeding.graph.execution.AiExecutionNodeResult;
import com.wens.breeding.lark.base.AiBaseWriteClient;

public final class PersistAnalysisResultNode implements AiExecutionNode {
    private final AiBaseWriteClient writeClient;

    public PersistAnalysisResultNode(AiBaseWriteClient writeClient) {
        this.writeClient = Objects.requireNonNull(writeClient, "writeClient");
    }

    @Override
    public String name() {
        return "persist-analysis-result";
    }

    @Override
    public AiExecutionNodeResult execute(AiExecutionContext context) {
        if (!context.hasAnalysisResult()) {
            return AiExecutionNodeResult.skipped("No analysis result to persist");
        }
        writeClient.saveAnalysisResult(context.getAnalysisResult());
        return AiExecutionNodeResult.completed("Analysis result persisted");
    }
}
