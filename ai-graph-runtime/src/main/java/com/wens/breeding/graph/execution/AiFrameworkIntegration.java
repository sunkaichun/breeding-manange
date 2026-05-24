package com.wens.breeding.graph.execution;

public final class AiFrameworkIntegration {
    private final AiFramework framework;
    private final String role;
    private final String adapterClassName;
    private final String status;
    private final String requiredJavaVersion;

    public AiFrameworkIntegration(
            AiFramework framework,
            String role,
            String adapterClassName,
            String status,
            String requiredJavaVersion) {
        this.framework = framework == null ? AiFramework.RULE_BASED : framework;
        this.role = requireText(role, "role");
        this.adapterClassName = requireText(adapterClassName, "adapterClassName");
        this.status = requireText(status, "status");
        this.requiredJavaVersion = requiredJavaVersion == null ? "" : requiredJavaVersion;
    }

    public AiFramework getFramework() {
        return framework;
    }

    public String getRole() {
        return role;
    }

    public String getAdapterClassName() {
        return adapterClassName;
    }

    public String getStatus() {
        return status;
    }

    public String getRequiredJavaVersion() {
        return requiredJavaVersion;
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value;
    }
}
