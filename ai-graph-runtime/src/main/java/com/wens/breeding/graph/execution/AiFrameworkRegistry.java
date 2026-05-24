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
                        "com.wens.breeding.graph.execution.NodeBasedAiExecutionEngine",
                        "java11-compatible-bridge",
                        "17+ for native langgraph4j"),
                new AiFrameworkIntegration(
                        AiFramework.LANGCHAIN4J,
                        "LLM and RAG model abstraction",
                        "com.wens.breeding.graph.llm.LlmGateway",
                        "gateway-port-ready",
                        "17+ for current native langchain4j"),
                new AiFrameworkIntegration(
                        AiFramework.SPRING_AI,
                        "Spring Boot AI client configuration and observability",
                        "com.wens.breeding.app.config.AiAppConfiguration",
                        "spring-bean-port-ready",
                        "17+ for Spring Boot 3 native integration"),
                new AiFrameworkIntegration(
                        AiFramework.OPENAI_SDK,
                        "Direct OpenAI model adapter",
                        "com.wens.breeding.app.openai.OpenAiLlmGateway",
                        "native-sdk-adapter",
                        "11+")));
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
