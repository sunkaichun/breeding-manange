package com.wens.breeding.lark.bot.runner;

import java.io.IOException;

public interface LarkEventConsumerProcessLauncher {
    LarkEventConsumerProcess start(LarkEventConsumerConfig config) throws IOException;
}
