package com.wens.breeding.lark.bot.runner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public final class RunningLarkEventConsumer {
    private final LarkEventConsumerConfig config;
    private final LarkEventConsumerProcess process;
    private final Thread stdoutThread;
    private final Thread stderrThread;
    private final AtomicBoolean ready;
    private final AtomicReference<Throwable> readerFailure;
    private final AtomicInteger exitCode = new AtomicInteger(Integer.MIN_VALUE);
    private final List<String> stderrLines;

    RunningLarkEventConsumer(
            LarkEventConsumerConfig config,
            LarkEventConsumerProcess process,
            Thread stdoutThread,
            Thread stderrThread,
            AtomicBoolean ready,
            AtomicReference<Throwable> readerFailure,
            List<String> stderrLines) {
        this.config = Objects.requireNonNull(config, "config");
        this.process = Objects.requireNonNull(process, "process");
        this.stdoutThread = Objects.requireNonNull(stdoutThread, "stdoutThread");
        this.stderrThread = Objects.requireNonNull(stderrThread, "stderrThread");
        this.ready = Objects.requireNonNull(ready, "ready");
        this.readerFailure = Objects.requireNonNull(readerFailure, "readerFailure");
        this.stderrLines = Objects.requireNonNull(stderrLines, "stderrLines");
    }

    public boolean isReady() {
        return ready.get();
    }

    public int awaitExit() {
        try {
            int code = process.waitFor();
            exitCode.set(code);
            joinReaders();
            throwReaderFailureIfPresent();
            return code;
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new LarkEventConsumerRunnerException("Interrupted while waiting for event consumer to exit", exception);
        }
    }

    public boolean awaitExit(long timeout, TimeUnit unit) {
        try {
            boolean exited = process.waitFor(timeout, unit);
            if (exited) {
                joinReaders();
                throwReaderFailureIfPresent();
            }
            return exited;
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new LarkEventConsumerRunnerException("Interrupted while waiting for event consumer to exit", exception);
        }
    }

    public void stop() {
        try {
            process.closeStdin();
        } catch (IOException exception) {
            process.destroy();
        }
    }

    public boolean isAlive() {
        return process.isAlive();
    }

    public int getExitCode() {
        return exitCode.get();
    }

    public List<String> getStderrLines() {
        synchronized (stderrLines) {
            return Collections.unmodifiableList(new ArrayList<>(stderrLines));
        }
    }

    public LarkEventConsumerConfig getConfig() {
        return config;
    }

    private void joinReaders() throws InterruptedException {
        stdoutThread.join(TimeUnit.SECONDS.toMillis(1));
        stderrThread.join(TimeUnit.SECONDS.toMillis(1));
    }

    private void throwReaderFailureIfPresent() {
        Throwable failure = readerFailure.get();
        if (failure != null) {
            throw new LarkEventConsumerRunnerException("Event consumer reader failed", failure);
        }
    }
}
