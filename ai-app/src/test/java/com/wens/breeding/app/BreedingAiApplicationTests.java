package com.wens.breeding.app;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "breeding.ai.provider=static",
        "breeding.ai.openai.enabled=false"
})
class BreedingAiApplicationTests {
    @Test
    void contextLoads() {
    }
}
