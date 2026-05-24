package com.wens.breeding.graph.execution;

public interface AiExecutionNode {
    String name();

    AiExecutionNodeResult execute(AiExecutionContext context);
}
