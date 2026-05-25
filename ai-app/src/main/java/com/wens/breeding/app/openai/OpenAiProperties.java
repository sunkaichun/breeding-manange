package com.wens.breeding.app.openai;

import java.time.Duration;

public final class OpenAiProperties {
    private boolean enabled;
    private String model = "gpt-5.2";
    private String apiKey = "";
    private String baseUrl = "";
    private String organization = "";
    private String project = "";
    private Duration timeout = Duration.ofSeconds(30);
    private int clientMaxRetries = 2;
    private int maxAttempts = 2;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public Duration getTimeout() {
        return timeout;
    }

    public void setTimeout(Duration timeout) {
        this.timeout = timeout;
    }

    public int getClientMaxRetries() {
        return clientMaxRetries;
    }

    public void setClientMaxRetries(int clientMaxRetries) {
        this.clientMaxRetries = clientMaxRetries;
    }

    public int getMaxAttempts() {
        return maxAttempts;
    }

    public void setMaxAttempts(int maxAttempts) {
        this.maxAttempts = maxAttempts;
    }

    public boolean hasApiKey() {
        return hasText(apiKey);
    }

    public boolean hasBaseUrl() {
        return hasText(baseUrl);
    }

    public boolean hasOrganization() {
        return hasText(organization);
    }

    public boolean hasProject() {
        return hasText(project);
    }

    private static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
