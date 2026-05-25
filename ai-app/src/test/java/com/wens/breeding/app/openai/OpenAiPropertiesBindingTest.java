package com.wens.breeding.app.openai;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Duration;

import com.wens.breeding.app.config.AiAppConfiguration;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.context.ConfigurationPropertiesAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class OpenAiPropertiesBindingTest {
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(ConfigurationPropertiesAutoConfiguration.class))
            .withUserConfiguration(AiAppConfiguration.class);

    @Test
    void bindsProxyProviderConfiguration() {
        contextRunner
                .withPropertyValues(
                        "breeding.ai.provider=static",
                        "breeding.ai.openai.api-key=proxy-key",
                        "breeding.ai.openai.base-url=https://proxy.example.com/v1",
                        "breeding.ai.openai.model=provider-model",
                        "breeding.ai.openai.organization=org_123",
                        "breeding.ai.openai.project=proj_123",
                        "breeding.ai.openai.timeout=45s",
                        "breeding.ai.openai.client-max-retries=4",
                        "breeding.ai.openai.max-attempts=3")
                .run(context -> {
                    OpenAiProperties properties = context.getBean(OpenAiProperties.class);

                    assertEquals("proxy-key", properties.getApiKey());
                    assertEquals("https://proxy.example.com/v1", properties.getBaseUrl());
                    assertEquals("provider-model", properties.getModel());
                    assertEquals("org_123", properties.getOrganization());
                    assertEquals("proj_123", properties.getProject());
                    assertEquals(Duration.ofSeconds(45), properties.getTimeout());
                    assertEquals(4, properties.getClientMaxRetries());
                    assertEquals(3, properties.getMaxAttempts());
                });
    }
}
