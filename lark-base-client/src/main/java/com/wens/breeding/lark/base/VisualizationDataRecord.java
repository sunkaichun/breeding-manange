package com.wens.breeding.lark.base;

public final class VisualizationDataRecord {
    private final String requestId;
    private final String chartType;
    private final String dimension;
    private final String metric;
    private final String dataJson;

    public VisualizationDataRecord(
            String requestId,
            String chartType,
            String dimension,
            String metric,
            String dataJson) {
        this.requestId = requireText(requestId, "requestId");
        this.chartType = requireText(chartType, "chartType");
        this.dimension = requireText(dimension, "dimension");
        this.metric = requireText(metric, "metric");
        this.dataJson = requireText(dataJson, "dataJson");
    }

    public String getRequestId() {
        return requestId;
    }

    public String getChartType() {
        return chartType;
    }

    public String getDimension() {
        return dimension;
    }

    public String getMetric() {
        return metric;
    }

    public String getDataJson() {
        return dataJson;
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value;
    }
}
