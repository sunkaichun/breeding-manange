package com.wens.breeding.mysql;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wens.breeding.analysis.model.AnalysisRequest;
import com.wens.breeding.analysis.model.AnalysisResult;
import com.wens.breeding.analysis.model.AnalysisType;
import com.wens.breeding.analysis.model.RequestSource;
import com.wens.breeding.analysis.model.RiskLevel;
import com.wens.breeding.lark.base.AiBaseWriteClient;
import com.wens.breeding.lark.base.VisualizationDataRecord;

import org.springframework.jdbc.core.JdbcTemplate;

public final class MysqlAiBaseWriteClient implements AiBaseWriteClient {
    private static final TypeReference<List<String>> STRING_LIST = new TypeReference<>() {
    };

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public MysqlAiBaseWriteClient(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
        this.jdbcTemplate = Objects.requireNonNull(jdbcTemplate, "jdbcTemplate");
        this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper");
    }

    @Override
    public String createAnalysisRequest(AnalysisRequest request) {
        AnalysisRequest value = Objects.requireNonNull(request, "request");
        jdbcTemplate.update(
                "INSERT INTO analysis_requests "
                        + "(request_id, source, requester_open_id, batch_id, analysis_type, start_date, end_date, raw_question) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?) "
                        + "ON DUPLICATE KEY UPDATE source = VALUES(source), requester_open_id = VALUES(requester_open_id), "
                        + "batch_id = VALUES(batch_id), analysis_type = VALUES(analysis_type), start_date = VALUES(start_date), "
                        + "end_date = VALUES(end_date), raw_question = VALUES(raw_question)",
                value.getRequestId(),
                value.getSource().name(),
                value.getRequesterOpenId(),
                value.getBatchId(),
                value.getAnalysisType().name(),
                Date.valueOf(value.getStartDate()),
                Date.valueOf(value.getEndDate()),
                value.getRawQuestion());
        return value.getRequestId();
    }

    @Override
    public String saveAnalysisResult(AnalysisResult result) {
        AnalysisResult value = Objects.requireNonNull(result, "result");
        jdbcTemplate.update(
                "INSERT INTO analysis_results (request_id, risk_level, summary, reasons_json, suggestions_json) "
                        + "VALUES (?, ?, ?, ?, ?) "
                        + "ON DUPLICATE KEY UPDATE risk_level = VALUES(risk_level), summary = VALUES(summary), "
                        + "reasons_json = VALUES(reasons_json), suggestions_json = VALUES(suggestions_json)",
                value.getRequestId(),
                value.getRiskLevel().name(),
                value.getSummary(),
                toJson(value.getReasons()),
                toJson(value.getSuggestions()));
        return value.getRequestId();
    }

    @Override
    public String saveVisualizationData(VisualizationDataRecord visualizationData) {
        VisualizationDataRecord value = Objects.requireNonNull(visualizationData, "visualizationData");
        String recordId = "viz-" + UUID.randomUUID();
        jdbcTemplate.update(
                "INSERT INTO visualization_data_records "
                        + "(record_id, request_id, chart_type, dimension_name, metric_name, data_json) "
                        + "VALUES (?, ?, ?, ?, ?, ?)",
                recordId,
                value.getRequestId(),
                value.getChartType(),
                value.getDimension(),
                value.getMetric(),
                value.getDataJson());
        return recordId;
    }

    public Optional<AnalysisRequest> findAnalysisRequest(String requestId) {
        List<AnalysisRequest> rows = jdbcTemplate.query(
                "SELECT request_id, source, requester_open_id, batch_id, analysis_type, start_date, end_date, raw_question "
                        + "FROM analysis_requests WHERE request_id = ?",
                MysqlAiBaseWriteClient::mapAnalysisRequest,
                requireText(requestId, "requestId"));
        return rows.stream().findFirst();
    }

    public Optional<AnalysisResult> findAnalysisResult(String requestId) {
        List<AnalysisResult> rows = jdbcTemplate.query(
                "SELECT request_id, risk_level, summary, reasons_json, suggestions_json "
                        + "FROM analysis_results WHERE request_id = ?",
                this::mapAnalysisResult,
                requireText(requestId, "requestId"));
        return rows.stream().findFirst();
    }

    public Optional<VisualizationDataRecord> findVisualizationDataRecord(String recordId) {
        List<VisualizationDataRecord> rows = jdbcTemplate.query(
                "SELECT request_id, chart_type, dimension_name, metric_name, data_json "
                        + "FROM visualization_data_records WHERE record_id = ?",
                MysqlAiBaseWriteClient::mapVisualizationDataRecord,
                requireText(recordId, "recordId"));
        return rows.stream().findFirst();
    }

    private static AnalysisRequest mapAnalysisRequest(ResultSet rs, int rowNum) throws SQLException {
        return new AnalysisRequest(
                rs.getString("request_id"),
                RequestSource.valueOf(rs.getString("source")),
                rs.getString("requester_open_id"),
                rs.getString("batch_id"),
                AnalysisType.valueOf(rs.getString("analysis_type")),
                rs.getDate("start_date").toLocalDate(),
                rs.getDate("end_date").toLocalDate(),
                rs.getString("raw_question"));
    }

    private AnalysisResult mapAnalysisResult(ResultSet rs, int rowNum) throws SQLException {
        return new AnalysisResult(
                rs.getString("request_id"),
                RiskLevel.valueOf(rs.getString("risk_level")),
                rs.getString("summary"),
                fromJsonList(rs.getString("reasons_json")),
                fromJsonList(rs.getString("suggestions_json")));
    }

    private static VisualizationDataRecord mapVisualizationDataRecord(ResultSet rs, int rowNum) throws SQLException {
        return new VisualizationDataRecord(
                rs.getString("request_id"),
                rs.getString("chart_type"),
                rs.getString("dimension_name"),
                rs.getString("metric_name"),
                rs.getString("data_json"));
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("Failed to serialize JSON value", exception);
        }
    }

    private List<String> fromJsonList(String value) {
        try {
            return objectMapper.readValue(value, STRING_LIST);
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("Failed to parse JSON list", exception);
        }
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value;
    }
}
