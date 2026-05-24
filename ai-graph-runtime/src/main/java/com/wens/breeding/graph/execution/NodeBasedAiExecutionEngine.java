package com.wens.breeding.graph.execution;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.wens.breeding.analysis.model.AnalysisRequest;
import com.wens.breeding.graph.AnalysisGraph;
import com.wens.breeding.graph.AnalysisGraphException;

public final class NodeBasedAiExecutionEngine implements AiExecutionEngine, AnalysisGraph {
    private final AiFramework framework;
    private final List<AiExecutionNode> nodes;

    public NodeBasedAiExecutionEngine(AiFramework framework, List<AiExecutionNode> nodes) {
        this.framework = framework == null ? AiFramework.LANGGRAPH4J : framework;
        if (nodes == null || nodes.isEmpty()) {
            throw new IllegalArgumentException("nodes must not be empty");
        }
        this.nodes = Collections.unmodifiableList(new ArrayList<>(nodes));
    }

    @Override
    public AiExecutionResult execute(AnalysisRequest request) {
        Objects.requireNonNull(request, "request");
        AiExecutionContext context = new AiExecutionContext(request);
        List<AiExecutionTrace> traces = new ArrayList<>();

        for (AiExecutionNode node : nodes) {
            try {
                AiExecutionNodeResult nodeResult = node.execute(context);
                traces.add(new AiExecutionTrace(node.name(), nodeResult.getStatus(), nodeResult.getMessage()));
            } catch (RuntimeException exception) {
                traces.add(new AiExecutionTrace(node.name(), AiExecutionNodeStatus.FAILED, exception.getMessage()));
                throw new AnalysisGraphException("AI execution node failed: " + node.name(), exception);
            }
        }

        if (!context.hasAnalysisResult()) {
            throw new AnalysisGraphException("AI execution finished without an analysis result");
        }
        return new AiExecutionResult(framework, context.getAnalysisResult(), traces);
    }

    @Override
    public com.wens.breeding.analysis.model.AnalysisResult run(AnalysisRequest request) {
        return execute(request).getAnalysisResult();
    }
}
