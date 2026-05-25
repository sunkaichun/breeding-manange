package com.wens.breeding.graph.execution;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class AiFrameworkRegistry {
    private final List<AiFrameworkIntegration> integrations;

    public AiFrameworkRegistry() {
        this(Arrays.asList(
                new AiFrameworkIntegration(
                        AiFramework.LANGGRAPH4J,
                        "State graph orchestration for analysis tasks",
                        "com.wens.breeding.graph.execution.NativeLangGraph4jExecutionEngine",
                        "native-langgraph4j-engine",
                        "21+ runtime baseline"),
                new AiFrameworkIntegration(
                        AiFramework.LANGCHAIN4J,
                        "LLM and RAG model abstraction",
                        "com.wens.breeding.graph.llm.LangChain4jLlmGateway",
                        "native-langchain4j-gateway",
                        "21+ runtime baseline"),
                new AiFrameworkIntegration(
                        AiFramework.SPRING_AI,
                        "Spring Boot AI client configuration and observability",
                        "com.wens.breeding.app.springai.SpringAiLlmGateway",
                        "native-spring-ai-gateway",
                        "21+ runtime baseline"),
                new AiFrameworkIntegration(
                        AiFramework.OPENAI_SDK,
                        "Direct OpenAI model adapter",
                        "com.wens.breeding.app.openai.OpenAiLlmGateway",
                        "native-sdk-adapter",
                        "21+ runtime baseline")));
    }

    public AiFrameworkRegistry(List<AiFrameworkIntegration> integrations) {
        this.integrations = integrations == null
                ? Collections.emptyList()
                : Collections.unmodifiableList(integrations);
    }

    public List<AiFrameworkIntegration> listIntegrations() {
        return integrations;
    }
}
