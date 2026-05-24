package com.wens.breeding.task;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;

public final class AsyncTaskQueue<T> {
    private final TaskStore<T> taskStore;
    private final Executor executor;
    private final Clock clock;

    public AsyncTaskQueue(TaskStore<T> taskStore, Executor executor) {
        this(taskStore, executor, Clock.systemUTC());
    }

    public AsyncTaskQueue(TaskStore<T> taskStore, Executor executor, Clock clock) {
        this.taskStore = Objects.requireNonNull(taskStore, "taskStore");
        this.executor = Objects.requireNonNull(executor, "executor");
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    public TaskRecord<T> submit(String taskId, String sourceId, Callable<T> task) {
        String normalizedTaskId = requireText(taskId, "taskId");
        Callable<T> nonNullTask = Objects.requireNonNull(task, "task");
        TaskRecord<T> accepted = taskStore.createAccepted(normalizedTaskId, sourceId, now());
        executor.execute(() -> runTask(normalizedTaskId, nonNullTask));
        return accepted;
    }

    private void runTask(String taskId, Callable<T> task) {
        taskStore.markRunning(taskId, now());
        try {
            T result = task.call();
            taskStore.markCompleted(taskId, result, now());
        } catch (Exception exception) {
            taskStore.markFailed(taskId, exception.getMessage(), now());
        }
    }

    private Instant now() {
        return Instant.now(clock);
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value;
    }
}
