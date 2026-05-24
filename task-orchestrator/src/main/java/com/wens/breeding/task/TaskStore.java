package com.wens.breeding.task;

import java.time.Instant;
import java.util.Optional;

public interface TaskStore<T> {
    TaskRecord<T> createAccepted(String taskId, String sourceId, Instant createdAt);

    TaskRecord<T> markRunning(String taskId, Instant startedAt);

    TaskRecord<T> markCompleted(String taskId, T result, Instant completedAt);

    TaskRecord<T> markFailed(String taskId, String errorMessage, Instant completedAt);

    Optional<TaskRecord<T>> findByTaskId(String taskId);
}
