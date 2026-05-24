package com.wens.breeding.lark.bot.runner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

class LarkEventConsumerRunnerTest {
    @Test
    void startsAfterReadyMarkerAndDispatchesStdoutLines() {
        FakeProcess process = new FakeProcess(
                "{\"event_id\":\"evt-001\"}\n{\"event_id\":\"evt-002\"}\n",
                "[event] ready event_key=im.message.receive_v1\n[event] exited received 2 event(s) in 1s (reason: limit)\n",
                0);
        LarkEventConsumerRunner runner = new LarkEventConsumerRunner(config -> process);
        List<String> lines = new ArrayList<>();

        RunningLarkEventConsumer consumer = runner.start(config(), lines::add);
        int exitCode = consumer.awaitExit();

        assertTrue(consumer.isReady());
        assertEquals(0, exitCode);
        assertEquals(2, lines.size());
        assertEquals("{\"event_id\":\"evt-001\"}", lines.get(0));
        assertTrue(consumer.getStderrLines().get(0).contains("ready"));
    }

    @Test
    void failsWhenReadyMarkerIsMissing() {
        FakeProcess process = new FakeProcess("", "[event] startup failed\n", 1);
        LarkEventConsumerRunner runner = new LarkEventConsumerRunner(config -> process);

        assertThrows(LarkEventConsumerRunnerException.class, () -> runner.start(config(), line -> {
        }));
    }

    @Test
    void stopClosesProcessStdinForGracefulShutdown() {
        FakeProcess process = new FakeProcess(
                "",
                "[event] ready event_key=im.message.receive_v1\n",
                0);
        LarkEventConsumerRunner runner = new LarkEventConsumerRunner(config -> process);

        RunningLarkEventConsumer consumer = runner.start(config(), line -> {
        });
        consumer.stop();

        assertTrue(process.isStdinClosed());
        assertFalse(process.isDestroyed());
    }

    private static LarkEventConsumerConfig config() {
        return LarkEventConsumerConfig
                .receiveMessageDefaults()
                .readyTimeout(Duration.ofMillis(100))
                .build();
    }

    private static final class FakeProcess implements LarkEventConsumerProcess {
        private final InputStream stdout;
        private final InputStream stderr;
        private final CloseTrackingOutputStream stdin = new CloseTrackingOutputStream();
        private final int exitCode;
        private boolean destroyed;

        private FakeProcess(String stdout, String stderr, int exitCode) {
            this.stdout = new ByteArrayInputStream(stdout.getBytes(StandardCharsets.UTF_8));
            this.stderr = new ByteArrayInputStream(stderr.getBytes(StandardCharsets.UTF_8));
            this.exitCode = exitCode;
        }

        @Override
        public InputStream getStdout() {
            return stdout;
        }

        @Override
        public InputStream getStderr() {
            return stderr;
        }

        @Override
        public OutputStream getStdin() {
            return stdin;
        }

        @Override
        public int waitFor() {
            return exitCode;
        }

        @Override
        public boolean waitFor(long timeout, TimeUnit unit) {
            return true;
        }

        @Override
        public void destroy() {
            destroyed = true;
        }

        @Override
        public boolean isAlive() {
            return false;
        }

        private boolean isDestroyed() {
            return destroyed;
        }

        private boolean isStdinClosed() {
            return stdin.isClosed();
        }
    }

    private static final class CloseTrackingOutputStream extends ByteArrayOutputStream {
        private boolean closed;

        @Override
        public void close() {
            closed = true;
        }

        private boolean isClosed() {
            return closed;
        }
    }
}
