package com.wens.breeding.app.config;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;

import com.wens.breeding.analysis.model.BreedingBatch;
import com.wens.breeding.analysis.model.BreedingStandard;
import com.wens.breeding.analysis.model.FcrRecord;
import com.wens.breeding.analysis.model.FcrStandard;
import com.wens.breeding.analysis.model.WeightRecord;
import com.wens.breeding.app.agent.AgentChatClient;
import com.wens.breeding.app.agent.OpenAiStreamingChatClient;
import com.wens.breeding.app.agent.StaticStreamingChatClient;
import com.wens.breeding.app.openai.OpenAiLlmGateway;
import com.wens.breeding.app.openai.OpenAiProperties;
import com.wens.breeding.app.springai.SpringAiLlmGateway;
import com.wens.breeding.graph.AnalysisGraph;
import com.wens.breeding.graph.execution.BreedingAnalysisExecutionGraphFactory;
import com.wens.breeding.graph.llm.LlmGateway;
import com.wens.breeding.graph.llm.RetryingLlmGateway;
import com.wens.breeding.graph.llm.StaticJsonLlmGateway;
import com.wens.breeding.lark.base.InMemoryBreedingBaseClient;
import com.wens.breeding.visualization.WeightTrendVisualizationGenerator;
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiAppConfiguration {
    @Bean
    public InMemoryBreedingBaseClient inMemoryBreedingBaseClient() {
        return new InMemoryBreedingBaseClient(
                Collections.singletonList(batch()),
                Arrays.asList(
                        weight("2026-05-20", 50, "1.42", "82"),
                        weight("2026-05-21", 51, "1.20", "76"),
                        weight("2026-05-22", 52, "1.22", "77")),
                Collections.singletonList(weightStandard()),
                Arrays.asList(
                        fcr("2026-05-21", 51, "150", "80"),
                        fcr("2026-05-22", 52, "152", "80")),
                Collections.singletonList(fcrStandard()));
    }

    @Bean
    public AnalysisGraph analysisGraph(InMemoryBreedingBaseClient baseClient) {
        return BreedingAnalysisExecutionGraphFactory.nativeLangGraph4j(baseClient, baseClient, baseClient);
    }

    @Bean
    @ConfigurationProperties(prefix = "breeding.ai.openai")
    public OpenAiProperties openAiProperties() {
        return new OpenAiProperties();
    }

    @Bean
    @ConfigurationProperties(prefix = "breeding.ai")
    public AiModelProperties aiModelProperties() {
        return new AiModelProperties();
    }

    @Bean
    public LlmGateway llmGateway(
            AiModelProperties aiModelProperties,
            OpenAiProperties openAiProperties,
            ObjectProvider<ChatModel> springAiChatModel) {
        LlmGateway gateway;
        switch (resolveProvider(aiModelProperties, openAiProperties)) {
            case OPENAI:
                gateway = new OpenAiLlmGateway(openAiClient(openAiProperties), openAiProperties.getModel());
                break;
            case SPRING_AI:
                gateway = new SpringAiLlmGateway(
                        requireSpringAiChatModel(springAiChatModel),
                        openAiProperties.getModel());
                break;
            case STATIC:
            default:
                gateway = new StaticJsonLlmGateway(aiModelProperties.getStaticJsonResponse());
                break;
        }
        return new RetryingLlmGateway(gateway, maxAttempts(aiModelProperties, openAiProperties));
    }

    @Bean
    public AgentChatClient agentChatClient(AiModelProperties aiModelProperties, OpenAiProperties openAiProperties) {
        if (resolveProvider(aiModelProperties, openAiProperties) == AiModelProvider.OPENAI) {
            return new OpenAiStreamingChatClient(openAiClient(openAiProperties), openAiProperties.getModel());
        }
        return new StaticStreamingChatClient();
    }

    @Bean
    public WeightTrendVisualizationGenerator weightTrendVisualizationGenerator() {
        return new WeightTrendVisualizationGenerator();
    }

    private static OpenAIClient openAiClient(OpenAiProperties openAiProperties) {
        OpenAIOkHttpClient.Builder builder = OpenAIOkHttpClient.builder()
                .fromEnv()
                .timeout(openAiProperties.getTimeout())
                .maxRetries(Math.max(0, openAiProperties.getClientMaxRetries()));
        if (openAiProperties.hasApiKey()) {
            builder.apiKey(openAiProperties.getApiKey());
        }
        if (openAiProperties.hasBaseUrl()) {
            builder.baseUrl(openAiProperties.getBaseUrl());
        }
        if (openAiProperties.hasOrganization()) {
            builder.organization(openAiProperties.getOrganization());
        }
        if (openAiProperties.hasProject()) {
            builder.project(openAiProperties.getProject());
        }
        return builder.build();
    }

    private static AiModelProvider resolveProvider(AiModelProperties aiModelProperties, OpenAiProperties openAiProperties) {
        if (openAiProperties.isEnabled() && aiModelProperties.getProvider() == AiModelProvider.STATIC) {
            return AiModelProvider.OPENAI;
        }
        return aiModelProperties.getProvider();
    }

    private static int maxAttempts(AiModelProperties aiModelProperties, OpenAiProperties openAiProperties) {
        int maxAttempts = aiModelProperties.getMaxAttempts();
        if (openAiProperties.isEnabled() && aiModelProperties.getProvider() == AiModelProvider.STATIC) {
            maxAttempts = openAiProperties.getMaxAttempts();
        }
        return Math.max(1, maxAttempts);
    }

    private static ChatModel requireSpringAiChatModel(ObjectProvider<ChatModel> springAiChatModel) {
        ChatModel chatModel = springAiChatModel.getIfAvailable();
        if (chatModel == null) {
            throw new IllegalStateException("breeding.ai.provider=SPRING_AI requires a Spring AI ChatModel bean");
        }
        return chatModel;
    }

    private static BreedingBatch batch() {
        return new BreedingBatch(
                "BATCH-001",
                "Org-A",
                "Datu2",
                "Mixed",
                LocalDate.parse("2026-04-01"),
                LocalDate.parse("2026-06-20"),
                "ou_test",
                "Owner");
    }

    private static WeightRecord weight(String measuredDate, int ageDays, String averageWeightKg, String uniformityPercent) {
        return new WeightRecord(
                "BATCH-001",
                LocalDate.parse(measuredDate),
                ageDays,
                new BigDecimal(averageWeightKg),
                new BigDecimal(uniformityPercent),
                1000);
    }

    private static BreedingStandard weightStandard() {
        return new BreedingStandard(
                "Datu2",
                "Mixed",
                31,
                60,
                new BigDecimal("1.30"),
                new BigDecimal("1.58"),
                new BigDecimal("80"));
    }

    private static FcrRecord fcr(String recordDate, int ageDays, String feedConsumedKg, String weightGainKg) {
        return new FcrRecord(
                "BATCH-001",
                LocalDate.parse(recordDate),
                ageDays,
                new BigDecimal(feedConsumedKg),
                new BigDecimal(weightGainKg));
    }

    private static FcrStandard fcrStandard() {
        return new FcrStandard(
                "Datu2",
                "Mixed",
                31,
                60,
                new BigDecimal("1.70"));
    }
}
