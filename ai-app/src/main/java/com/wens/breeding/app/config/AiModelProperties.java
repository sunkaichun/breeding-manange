package com.wens.breeding.app.config;

public final class AiModelProperties {
    private AiModelProvider provider = AiModelProvider.STATIC;
    private int maxAttempts = 2;
    private String staticJsonResponse = "{\"answer\":\"AI model integration is disabled.\",\"citations\":[]}";

    public AiModelProvider getProvider() {
        return provider;
    }

    public void setProvider(AiModelProvider provider) {
        this.provider = provider == null ? AiModelProvider.STATIC : provider;
    }

    public int getMaxAttempts() {
        return maxAttempts;
    }

    public void setMaxAttempts(int maxAttempts) {
        this.maxAttempts = maxAttempts;
    }

    public String getStaticJsonResponse() {
        return staticJsonResponse;
    }

    public void setStaticJsonResponse(String staticJsonResponse) {
        this.staticJsonResponse = staticJsonResponse;
    }
}
