package com.wens.breeding.graph.execution;

import java.util.Arrays;

import com.wens.breeding.analysis.rule.FcrAnalyzer;
import com.wens.breeding.analysis.rule.UniformityAnalyzer;
import com.wens.breeding.analysis.rule.WeightTrendAnalyzer;
import com.wens.breeding.graph.execution.node.LoadBatchNode;
import com.wens.breeding.graph.execution.node.PersistAnalysisRequestNode;
import com.wens.breeding.graph.execution.node.PersistAnalysisResultNode;
import com.wens.breeding.graph.execution.node.RuleAnalysisNode;
import com.wens.breeding.lark.base.AiBaseWriteClient;
import com.wens.breeding.lark.base.BreedingBaseClient;
import com.wens.breeding.lark.base.FcrBaseClient;

public final class BreedingAnalysisExecutionGraphFactory {
    private BreedingAnalysisExecutionGraphFactory() {
    }

    public static NodeBasedAiExecutionEngine langGraphStyle(
            BreedingBaseClient breedingBaseClient,
            FcrBaseClient fcrBaseClient,
            AiBaseWriteClient writeClient) {
        return new NodeBasedAiExecutionEngine(
                AiFramework.LANGGRAPH4J,
                breedingAnalysisNodes(breedingBaseClient, fcrBaseClient, writeClient));
    }

    public static NativeLangGraph4jExecutionEngine nativeLangGraph4j(
            BreedingBaseClient breedingBaseClient,
            FcrBaseClient fcrBaseClient,
            AiBaseWriteClient writeClient) {
        return new NativeLangGraph4jExecutionEngine(
                breedingAnalysisNodes(breedingBaseClient, fcrBaseClient, writeClient));
    }

    private static java.util.List<AiExecutionNode> breedingAnalysisNodes(
            BreedingBaseClient breedingBaseClient,
            FcrBaseClient fcrBaseClient,
            AiBaseWriteClient writeClient) {
        return Arrays.asList(
                new PersistAnalysisRequestNode(writeClient),
                new LoadBatchNode(breedingBaseClient),
                new RuleAnalysisNode(
                        breedingBaseClient,
                        fcrBaseClient,
                        new WeightTrendAnalyzer(),
                        new UniformityAnalyzer(),
                        new FcrAnalyzer()),
                new PersistAnalysisResultNode(writeClient));
    }
}
