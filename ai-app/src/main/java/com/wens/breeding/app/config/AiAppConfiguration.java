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
import com.wens.breeding.app.openai.OpenAiLlmGateway;
import com.wens.breeding.app.openai.OpenAiProperties;
import com.wens.breeding.graph.AnalysisGraph;
import com.wens.breeding.graph.execution.BreedingAnalysisExecutionGraphFactory;
import com.wens.breeding.graph.llm.LlmGateway;
import com.wens.breeding.graph.llm.RetryingLlmGateway;
import com.wens.breeding.graph.llm.StaticJsonLlmGateway;
import com.wens.breeding.lark.base.InMemoryBreedingBaseClient;
import com.wens.breeding.visualization.WeightTrendVisualizationGenerator;
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;

import org.springframework.boot.context.properties.ConfigurationProperties;
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
        return BreedingAnalysisExecutionGraphFactory.langGraphStyle(baseClient, baseClient, baseClient);
    }

    @Bean
    @ConfigurationProperties(prefix = "breeding.ai.openai")
    public OpenAiProperties openAiProperties() {
        return new OpenAiProperties();
    }

    @Bean
    public LlmGateway llmGateway(OpenAiProperties openAiProperties) {
        if (!openAiProperties.isEnabled()) {
            return new StaticJsonLlmGateway("{\"answer\":\"OpenAI integration is disabled.\",\"citations\":[]}");
        }
        OpenAIClient client = openAiClient(openAiProperties);
        return new RetryingLlmGateway(
                new OpenAiLlmGateway(client, openAiProperties.getModel()),
                Math.max(1, openAiProperties.getMaxAttempts()));
    }

    @Bean
    public WeightTrendVisualizationGenerator weightTrendVisualizationGenerator() {
        return new WeightTrendVisualizationGenerator();
    }

    private static OpenAIClient openAiClient(OpenAiProperties openAiProperties) {
        if (openAiProperties.hasApiKey()) {
            return OpenAIOkHttpClient.builder()
                    .apiKey(openAiProperties.getApiKey())
                    .build();
        }
        return OpenAIOkHttpClient.fromEnv();
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
