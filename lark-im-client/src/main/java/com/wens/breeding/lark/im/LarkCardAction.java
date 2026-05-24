package com.wens.breeding.lark.im;

public final class LarkCardAction {
    private final String label;
    private final String url;

    public LarkCardAction(String label, String url) {
        this.label = Texts.requireText(label, "label");
        this.url = Texts.requireText(url, "url");
    }

    public String getLabel() {
        return label;
    }

    public String getUrl() {
        return url;
    }
}
