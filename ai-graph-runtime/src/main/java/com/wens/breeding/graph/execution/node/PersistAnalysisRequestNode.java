package com.wens.breeding.graph.execution.node;

import java.util.Objects;

import com.wens.breeding.graph.execution.AiExecutionContext;
import com.wens.breeding.graph.execution.AiExecutionNode;
import com.wens.breeding.graph.execution.AiExecutionNodeResult;
import com.wens.breeding.lark.base.AiBaseWriteClient;

public final class PersistAnalysisRequestNode implements AiExecutionNode {
    private final AiBaseWriteClient writeClient;

    public PersistAnalysisRequestNode(AiBaseWriteClient writeClient) {
        this.writeClient = Objects.requireNonNull(writeClient, "writeClient");
    }

    @Override
    public String name() {
        return "persist-analysis-request";
    }

    @Override
    public AiExecutionNodeResult execute(AiExecutionContext context) {
        writeClient.createAnalysisRequest(context.getRequest());
        return AiExecutionNodeResult.completed("Analysis request persisted");
    }
}
