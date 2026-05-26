package com.wens.breeding.app.config;

public final class StorageProperties {
    private String provider = "memory";

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider == null || provider.trim().isEmpty() ? "memory" : provider.trim().toLowerCase();
    }

    public boolean isMysql() {
        return "mysql".equals(provider);
    }
}
