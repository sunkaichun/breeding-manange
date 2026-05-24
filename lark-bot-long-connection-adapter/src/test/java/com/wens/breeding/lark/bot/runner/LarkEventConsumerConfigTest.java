package com.wens.breeding.lark.bot.runner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.time.Duration;
import java.util.Arrays;

import org.junit.jupiter.api.Test;

class LarkEventConsumerConfigTest {
    @Test
    void buildsReceiveMessageConsumeCommandWithoutQuietFlag() {
        LarkEventConsumerConfig config = LarkEventConsumerConfig
                .receiveMessageDefaults()
                .maxEvents(1)
                .timeout("30s")
                .readyTimeout(Duration.ofMillis(100))
                .build();

        assertEquals(Arrays.asList(
                "lark-cli",
                "event",
                "consume",
                "im.message.receive_v1",
                "--as",
                "bot",
                "--max-events",
                "1",
                "--timeout",
                "30s"), config.toCommand());
        assertFalse(config.toCommand().contains("--quiet"));
    }
}
