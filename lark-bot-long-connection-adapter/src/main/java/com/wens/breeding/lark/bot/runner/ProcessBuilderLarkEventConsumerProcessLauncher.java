package com.wens.breeding.lark.bot.runner;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.TimeUnit;

public final class ProcessBuilderLarkEventConsumerProcessLauncher implements LarkEventConsumerProcessLauncher {
    @Override
    public LarkEventConsumerProcess start(LarkEventConsumerConfig config) throws IOException {
        Process process = new ProcessBuilder(config.toCommand()).start();
        return new ProcessBackedLarkEventConsumerProcess(process);
    }

    private static final class ProcessBackedLarkEventConsumerProcess implements LarkEventConsumerProcess {
        private final Process process;

        private ProcessBackedLarkEventConsumerProcess(Process process) {
            this.process = process;
        }

        @Override
        public InputStream getStdout() {
            return process.getInputStream();
        }

        @Override
        public InputStream getStderr() {
            return process.getErrorStream();
        }

        @Override
        public OutputStream getStdin() {
            return process.getOutputStream();
        }

        @Override
        public int waitFor() throws InterruptedException {
            return process.waitFor();
        }

        @Override
        public boolean waitFor(long timeout, TimeUnit unit) throws InterruptedException {
            return process.waitFor(timeout, unit);
        }

        @Override
        public void destroy() {
            process.destroy();
        }

        @Override
        public boolean isAlive() {
            return process.isAlive();
        }
    }
}
