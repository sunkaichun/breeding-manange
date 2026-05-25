package com.wens.breeding.app.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;

import com.wens.breeding.lark.bot.runner.LarkEventConsumerConfig;
import com.wens.breeding.lark.bot.runner.LarkEventConsumerIdentity;

import org.junit.jupiter.api.Test;

import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.context.ConfigurationPropertiesAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class LarkBotPropertiesBindingTest {
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(ConfigurationPropertiesAutoConfiguration.class))
            .withUserConfiguration(AiAppConfiguration.class);

    @Test
    void bindsLarkBotAppAndConsumerConfiguration() {
        contextRunner
                .withPropertyValues(
                        "breeding.ai.provider=static",
                        "breeding.lark.bot.app.app-id=cli_app_id",
                        "breeding.lark.bot.app.app-secret=cli_secret",
                        "breeding.lark.bot.app.verification-token=verify_token",
                        "breeding.lark.bot.app.encrypt-key=encrypt_key",
                        "breeding.lark.bot.app.bot-open-id=ou_bot",
                        "breeding.lark.bot.consumer.enabled=true",
                        "breeding.lark.bot.consumer.cli-path=/usr/local/bin/lark-cli",
                        "breeding.lark.bot.consumer.event-key=im.message.receive_v1",
                        "breeding.lark.bot.consumer.identity=USER",
                        "breeding.lark.bot.consumer.max-events=3",
                        "breeding.lark.bot.consumer.timeout=60s",
                        "breeding.lark.bot.consumer.jq-expression=.",
                        "breeding.lark.bot.consumer.ready-timeout=15s",
                        "breeding.lark.bot.queue-delay=800ms",
                        "breeding.lark.bot.queue-threads=4")
                .run(context -> {
                    LarkBotProperties properties = context.getBean(LarkBotProperties.class);
                    LarkEventConsumerConfig consumerConfig = context.getBean(LarkEventConsumerConfig.class);

                    assertEquals("cli_app_id", properties.getApp().getAppId());
                    assertEquals("cli_secret", properties.getApp().getAppSecret());
                    assertEquals("verify_token", properties.getApp().getVerificationToken());
                    assertEquals("encrypt_key", properties.getApp().getEncryptKey());
                    assertEquals("ou_bot", properties.getApp().getBotOpenId());
                    assertTrue(properties.getApp().hasAppCredentials());
                    assertEquals(Duration.ofMillis(800), properties.getQueueDelay());
                    assertEquals(4, properties.resolvedQueueThreads());

                    assertEquals("/usr/local/bin/lark-cli", consumerConfig.getCliPath());
                    assertEquals("im.message.receive_v1", consumerConfig.getEventKey());
                    assertEquals(LarkEventConsumerIdentity.USER, consumerConfig.getIdentity());
                    assertEquals(3, consumerConfig.getMaxEvents());
                    assertEquals("60s", consumerConfig.getTimeout());
                    assertEquals(".", consumerConfig.getJqExpression());
                    assertEquals(Duration.ofSeconds(15), consumerConfig.getReadyTimeout());
                });
    }
}
