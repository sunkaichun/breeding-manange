package com.wens.breeding.lark.bot.runner;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public final class LarkEventConsumerRunner {
    private final LarkEventConsumerProcessLauncher launcher;

    public LarkEventConsumerRunner(LarkEventConsumerProcessLauncher launcher) {
        this.launcher = Objects.requireNonNull(launcher, "launcher");
    }

    public RunningLarkEventConsumer start(LarkEventConsumerConfig config, EventLineHandler handler) {
        Objects.requireNonNull(config, "config");
        Objects.requireNonNull(handler, "handler");
        try {
            LarkEventConsumerProcess process = launcher.start(config);
            CountDownLatch readyLatch = new CountDownLatch(1);
            AtomicBoolean ready = new AtomicBoolean(false);
            AtomicReference<Throwable> readerFailure = new AtomicReference<>();
            List<String> stderrLines = Collections.synchronizedList(new ArrayList<String>());

            Thread stderrThread = new Thread(
                    () -> drainStderr(process, config, readyLatch, ready, readerFailure, stderrLines),
                    "lark-event-consumer-stderr-" + config.getEventKey());
            Thread stdoutThread = new Thread(
                    () -> drainStdout(process, handler, readerFailure),
                    "lark-event-consumer-stdout-" + config.getEventKey());
            stderrThread.setDaemon(true);
            stdoutThread.setDaemon(true);
            stderrThread.start();

            awaitReady(config, readyLatch, ready, readerFailure, stderrLines);
            stdoutThread.start();
            return new RunningLarkEventConsumer(
                    config,
                    process,
                    stdoutThread,
                    stderrThread,
                    ready,
                    readerFailure,
                    stderrLines);
        } catch (IOException exception) {
            throw new LarkEventConsumerRunnerException("Failed to start lark event consumer", exception);
        }
    }

    private static void awaitReady(
            LarkEventConsumerConfig config,
            CountDownLatch readyLatch,
            AtomicBoolean ready,
            AtomicReference<Throwable> readerFailure,
            List<String> stderrLines) {
        try {
            boolean signaled = readyLatch.await(config.getReadyTimeout().toMillis(), TimeUnit.MILLISECONDS);
            if (!signaled || !ready.get()) {
                Throwable failure = readerFailure.get();
                if (failure != null) {
                    throw new LarkEventConsumerRunnerException("Event consumer failed before ready marker", failure);
                }
                throw new LarkEventConsumerRunnerException(
                        "Timed out waiting for ready marker for " + config.getEventKey()
                                + "; stderr=" + stderrLines);
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new LarkEventConsumerRunnerException("Interrupted while waiting for ready marker", exception);
        }
    }

    private static void drainStderr(
            LarkEventConsumerProcess process,
            LarkEventConsumerConfig config,
            CountDownLatch readyLatch,
            AtomicBoolean ready,
            AtomicReference<Throwable> readerFailure,
            List<String> stderrLines) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                process.getStderr(),
                StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                stderrLines.add(line);
                if (isReadyMarker(line, config.getEventKey())) {
                    ready.set(true);
                    readyLatch.countDown();
                }
            }
            if (!ready.get()) {
                readerFailure.compareAndSet(
                        null,
                        new LarkEventConsumerRunnerException("stderr closed before ready marker"));
                readyLatch.countDown();
            }
        } catch (Throwable throwable) {
            readerFailure.compareAndSet(null, throwable);
            readyLatch.countDown();
        }
    }

    private static void drainStdout(
            LarkEventConsumerProcess process,
            EventLineHandler handler,
            AtomicReference<Throwable> readerFailure) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                process.getStdout(),
                StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    handler.onLine(line);
                }
            }
        } catch (Throwable throwable) {
            readerFailure.compareAndSet(null, throwable);
        }
    }

    static boolean isReadyMarker(String line, String eventKey) {
        return ("[event] ready event_key=" + eventKey).equals(line);
    }
}
