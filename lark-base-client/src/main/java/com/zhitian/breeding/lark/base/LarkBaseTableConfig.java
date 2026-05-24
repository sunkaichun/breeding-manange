package com.zhitian.breeding.lark.base;

public final class LarkBaseTableConfig {
    private final String appToken;
    private final String batchTableId;
    private final String weightRecordTableId;
    private final String standardTableId;

    public LarkBaseTableConfig(
            String appToken,
            String batchTableId,
            String weightRecordTableId,
            String standardTableId) {
        this.appToken = requireText(appToken, "appToken");
        this.batchTableId = requireText(batchTableId, "batchTableId");
        this.weightRecordTableId = requireText(weightRecordTableId, "weightRecordTableId");
        this.standardTableId = requireText(standardTableId, "standardTableId");
    }

    public String getAppToken() {
        return appToken;
    }

    public String getBatchTableId() {
        return batchTableId;
    }

    public String getWeightRecordTableId() {
        return weightRecordTableId;
    }

    public String getStandardTableId() {
        return standardTableId;
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value;
    }
}
