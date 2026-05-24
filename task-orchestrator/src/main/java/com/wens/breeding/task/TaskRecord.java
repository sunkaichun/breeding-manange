package com.wens.breeding.task;

import java.time.Instant;

public final class TaskRecord<T> {
    private final String taskId;
    private final String sourceId;
    private final TaskStatus status;
    private final T result;
    private final String errorMessage;
    private final Instant createdAt;
    private final Instant startedAt;
    private final Instant completedAt;

    public TaskRecord(
            String taskId,
            String sourceId,
            TaskStatus status,
            T result,
            String errorMessage,
            Instant createdAt,
            Instant startedAt,
            Instant completedAt) {
        this.taskId = requireText(taskId, "taskId");
        this.sourceId = sourceId == null ? "" : sourceId;
        this.status = status == null ? TaskStatus.ACCEPTED : status;
        this.result = result;
        this.errorMessage = errorMessage == null ? "" : errorMessage;
        this.createdAt = createdAt;
        this.startedAt = startedAt;
        this.completedAt = completedAt;
    }

    public static <T> TaskRecord<T> accepted(String taskId, String sourceId, Instant createdAt) {
        return new TaskRecord<>(taskId, sourceId, TaskStatus.ACCEPTED, null, "", createdAt, null, null);
    }

    public TaskRecord<T> running(Instant startedAt) {
        return new TaskRecord<>(taskId, sourceId, TaskStatus.RUNNING, result, "", createdAt, startedAt, null);
    }

    public TaskRecord<T> completed(T result, Instant completedAt) {
        return new TaskRecord<>(taskId, sourceId, TaskStatus.COMPLETED, result, "", createdAt, startedAt, completedAt);
    }

    public TaskRecord<T> failed(String errorMessage, Instant completedAt) {
        return new TaskRecord<>(taskId, sourceId, TaskStatus.FAILED, null, errorMessage, createdAt, startedAt, completedAt);
    }

    public String getTaskId() {
        return taskId;
    }

    public String getSourceId() {
        return sourceId;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public T getResult() {
        return result;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value;
    }
}
