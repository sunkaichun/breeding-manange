package com.wens.breeding.graph.llm;

public interface LlmGateway {
    LlmResponse complete(LlmRequest request);
}
