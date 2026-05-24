package com.zhitian.breeding.graph.llm;

public interface LlmGateway {
    LlmResponse complete(LlmRequest request);
}
