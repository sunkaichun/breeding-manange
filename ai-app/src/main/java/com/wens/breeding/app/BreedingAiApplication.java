package com.wens.breeding.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.wens.breeding")
public class BreedingAiApplication {
    public static void main(String[] args) {
        SpringApplication.run(BreedingAiApplication.class, args);
    }
}
