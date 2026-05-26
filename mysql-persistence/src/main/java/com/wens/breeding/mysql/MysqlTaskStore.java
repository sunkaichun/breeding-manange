package com.wens.breeding.mysql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wens.breeding.analysis.model.AnalysisResult;
import com.wens.breeding.analysis.model.RiskLevel;
import com.wens.breeding.task.TaskRecord;
import com.wens.breeding.task.TaskStatus;
import com.wens.breeding.task.TaskStore;

import org.springframework.jdbc.core.JdbcTemplate;

public final class MysqlTaskStore<T> implements TaskStore<T> {
    private static final TypeReference<List<String>> STRING_LIST = new TypeReference<>() {
    };

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;
    private final Class<T> resultType;

    public MysqlTaskStore(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper, Class<T> resultType) {
        this.jdbcTemplate = Objects.requireNonNull(jdbcTemplate, "jdbcTemplate");
        this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper");
        this.resultType = Objects.requireNonNull(resultType, "resultType");
    }

    @Override
    public TaskRecord<T> createAccepted(String taskId, String sourceId, Instant createdAt) {
        String normalizedTaskId = requireText(taskId, "taskId");
        TaskRecord<T> record = TaskRecord.accepted(normalizedTaskId, sourceId, createdAt);
        jdbcTemplate.update(
                "INSERT INTO task_records "
                        + "(task_id, source_id, status, result_json, result_type, error_message, created_at, started_at, completed_at) "
                        + "VALUES (?, ?, ?, NULL, ?, '', ?, NULL, NULL) "
                        + "ON DUPLICATE KEY UPDATE source_id = VALUES(source_id), status = VALUES(status), "
                        + "result_json = NULL, result_type = VALUES(result_type), error_message = '', "
                        + "created_at = VALUES(created_at), started_at = NULL, completed_at = NULL",
                normalizedTaskId,
                record.getSourceId(),
                record.getStatus().name(),
                resultType.getName(),
                toTimestamp(record.getCreatedAt()));
        return record;
    }

    @Override
    public TaskRecord<T> markRunning(String taskId, Instant startedAt) {
        TaskRecord<T> current = requireExisting(taskId).running(startedAt);
        jdbcTemplate.update(
                "UPDATE task_records SET status = ?, error_message = '', started_at = ?, completed_at = NULL WHERE task_id = ?",
                current.getStatus().name(),
                toTimestamp(current.getStartedAt()),
                current.getTaskId());
        return current;
    }

    @Override
    public TaskRecord<T> markCompleted(String taskId, T result, Instant completedAt) {
        TaskRecord<T> current = requireExisting(taskId).completed(result, completedAt);
        jdbcTemplate.update(
                "UPDATE task_records SET status = ?, result_json = ?, result_type = ?, "
                        + "error_message = '', completed_at = ? WHERE task_id = ?",
                current.getStatus().name(),
                toJson(current.getResult()),
                resultType.getName(),
                toTimestamp(current.getCompletedAt()),
                current.getTaskId());
        return current;
    }

    @Override
    public TaskRecord<T> markFailed(String taskId, String errorMessage, Instant completedAt) {
        TaskRecord<T> current = requireExisting(taskId).failed(errorMessage, completedAt);
        jdbcTemplate.update(
                "UPDATE task_records SET status = ?, result_json = NULL, error_message = ?, completed_at = ? WHERE task_id = ?",
                current.getStatus().name(),
                current.getErrorMessage(),
                toTimestamp(current.getCompletedAt()),
                current.getTaskId());
        return current;
    }

    @Override
    public Optional<TaskRecord<T>> findByTaskId(String taskId) {
        List<TaskRecord<T>> rows = jdbcTemplate.query(
                "SELECT task_id, source_id, status, result_json, error_message, created_at, started_at, completed_at "
                        + "FROM task_records WHERE task_id = ?",
                this::mapTaskRecord,
                requireText(taskId, "taskId"));
        return rows.stream().findFirst();
    }

    private TaskRecord<T> requireExisting(String taskId) {
        return findByTaskId(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task does not exist: " + taskId));
    }

    private TaskRecord<T> mapTaskRecord(ResultSet rs, int rowNum) throws SQLException {
        return new TaskRecord<>(
                rs.getString("task_id"),
                rs.getString("source_id"),
                TaskStatus.valueOf(rs.getString("status")),
                fromJson(rs.getString("result_json")),
                rs.getString("error_message"),
                toInstant(rs.getTimestamp("created_at")),
                toInstant(rs.getTimestamp("started_at")),
                toInstant(rs.getTimestamp("completed_at")));
    }

    private String toJson(T value) {
        if (value == null) {
            return "null";
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("Failed to serialize task result", exception);
        }
    }

    private T fromJson(String value) {
        if (value == null || value.isBlank() || "null".equals(value)) {
            return null;
        }
        try {
            if (AnalysisResult.class.equals(resultType)) {
                return resultType.cast(toAnalysisResult(value));
            }
            return objectMapper.readValue(value, resultType);
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("Failed to parse task result", exception);
        }
    }

    private AnalysisResult toAnalysisResult(String value) throws JsonProcessingException {
        JsonNode node = objectMapper.readTree(value);
        return new AnalysisResult(
                node.path("requestId").asText(),
                RiskLevel.valueOf(node.path("riskLevel").asText(RiskLevel.UNKNOWN.name())),
                node.path("summary").asText(),
                objectMapper.convertValue(node.path("reasons"), STRING_LIST),
                objectMapper.convertValue(node.path("suggestions"), STRING_LIST));
    }

    private static Timestamp toTimestamp(Instant value) {
        return value == null ? null : Timestamp.from(value);
    }

    private static Instant toInstant(Timestamp value) {
        return value == null ? null : value.toInstant();
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value;
    }
}
