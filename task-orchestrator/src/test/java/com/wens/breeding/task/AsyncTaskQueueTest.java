package com.wens.breeding.task;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

import org.junit.jupiter.api.Test;

class AsyncTaskQueueTest {
    private final Clock clock = Clock.fixed(Instant.parse("2026-05-24T00:00:00Z"), ZoneOffset.UTC);

    @Test
    void submitCreatesAcceptedTaskBeforeQueuedWorkRuns() {
        InMemoryTaskStore<String> store = new InMemoryTaskStore<>();
        ManualExecutor executor = new ManualExecutor();
        AsyncTaskQueue<String> queue = new AsyncTaskQueue<>(store, executor, clock);

        TaskRecord<String> accepted = queue.submit("task-001", "message-001", () -> "done");

        assertEquals(TaskStatus.ACCEPTED, accepted.getStatus());
        assertEquals(TaskStatus.ACCEPTED, store.findByTaskId("task-001").get().getStatus());

        executor.runNext();

        TaskRecord<String> completed = store.findByTaskId("task-001").get();
        assertEquals(TaskStatus.COMPLETED, completed.getStatus());
        assertEquals("done", completed.getResult());
        assertNotNull(completed.getStartedAt());
        assertNotNull(completed.getCompletedAt());
    }

    @Test
    void failedWorkMarksTaskFailed() {
        InMemoryTaskStore<String> store = new InMemoryTaskStore<>();
        ManualExecutor executor = new ManualExecutor();
        AsyncTaskQueue<String> queue = new AsyncTaskQueue<>(store, executor, clock);

        queue.submit("task-002", "message-002", () -> {
            throw new IllegalStateException("analysis failed");
        });

        executor.runNext();

        TaskRecord<String> failed = store.findByTaskId("task-002").get();
        assertEquals(TaskStatus.FAILED, failed.getStatus());
        assertEquals("analysis failed", failed.getErrorMessage());
    }

    private static final class ManualExecutor implements Executor {
        private final List<Runnable> tasks = new ArrayList<>();

        @Override
        public void execute(Runnable command) {
            tasks.add(command);
        }

        private void runNext() {
            tasks.remove(0).run();
        }
    }
}
