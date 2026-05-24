package com.wens.breeding.lark.im;

public final class LarkImClientModule {
    private LarkImClientModule() {
    }

    public static String name() {
        return "lark-im-client";
    }

    public static LarkImClient inMemoryClient() {
        return new InMemoryLarkImClient();
    }
}
