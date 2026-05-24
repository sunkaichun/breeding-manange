package com.wens.breeding.lark.bot.runner;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.TimeUnit;

public interface LarkEventConsumerProcess {
    InputStream getStdout();

    InputStream getStderr();

    OutputStream getStdin();

    int waitFor() throws InterruptedException;

    boolean waitFor(long timeout, TimeUnit unit) throws InterruptedException;

    void destroy();

    boolean isAlive();

    default void closeStdin() throws IOException {
        getStdin().close();
    }
}
