package com.wens.breeding.lark.bot;

import com.wens.breeding.lark.bot.runner.LarkEventConsumerRunner;
import com.wens.breeding.lark.bot.runner.ProcessBuilderLarkEventConsumerProcessLauncher;

public final class LarkBotLongConnectionAdapterModule {
    private LarkBotLongConnectionAdapterModule() {
    }

    public static String name() {
        return "lark-bot-long-connection-adapter";
    }

    public static LarkEventConsumerRunner defaultRunner() {
        return new LarkEventConsumerRunner(new ProcessBuilderLarkEventConsumerProcessLauncher());
    }
}
