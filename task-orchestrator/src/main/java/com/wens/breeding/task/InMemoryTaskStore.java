package com.wens.breeding.task;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class InMemoryTaskStore<T> implements TaskStore<T> {
    private final Map<String, TaskRecord<T>> records = new ConcurrentHashMap<>();

    @Override
    public TaskRecord<T> createAccepted(String taskId, String sourceId, Instant createdAt) {
        TaskRecord<T> record = TaskRecord.accepted(taskId, sourceId, createdAt);
        records.put(taskId, record);
        return record;
    }

    @Override
    public TaskRecord<T> markRunning(String taskId, Instant startedAt) {
        return records.compute(taskId, (key, current) -> requireExisting(key, current).running(startedAt));
    }

    @Override
    public TaskRecord<T> markCompleted(String taskId, T result, Instant completedAt) {
        return records.compute(taskId, (key, current) -> requireExisting(key, current).completed(result, completedAt));
    }

    @Override
    public TaskRecord<T> markFailed(String taskId, String errorMessage, Instant completedAt) {
        return records.compute(taskId, (key, current) -> requireExisting(key, current).failed(errorMessage, completedAt));
    }

    @Override
    public Optional<TaskRecord<T>> findByTaskId(String taskId) {
        return Optional.ofNullable(records.get(taskId));
    }

    private static <T> TaskRecord<T> requireExisting(String taskId, TaskRecord<T> record) {
        if (record == null) {
            throw new IllegalArgumentException("task does not exist: " + taskId);
        }
        return record;
    }
}
