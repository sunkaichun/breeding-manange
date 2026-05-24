package com.wens.breeding.lark.im;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class LarkCardMessage {
    private final String title;
    private final String summary;
    private final Map<String, String> fields;
    private final List<LarkCardAction> actions;

    public LarkCardMessage(
            String title,
            String summary,
            Map<String, String> fields,
            List<LarkCardAction> actions) {
        this.title = Texts.requireText(title, "title");
        this.summary = Texts.requireText(summary, "summary");
        this.fields = immutableFields(fields);
        this.actions = immutableActions(actions);
    }

    public static Builder builder(String title, String summary) {
        return new Builder(title, summary);
    }

    public String getTitle() {
        return title;
    }

    public String getSummary() {
        return summary;
    }

    public Map<String, String> getFields() {
        return fields;
    }

    public List<LarkCardAction> getActions() {
        return actions;
    }

    private static Map<String, String> immutableFields(Map<String, String> values) {
        if (values == null || values.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, String> copy = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : values.entrySet()) {
            copy.put(Texts.requireText(entry.getKey(), "field key"), Texts.requireText(entry.getValue(), "field value"));
        }
        return Collections.unmodifiableMap(copy);
    }

    private static List<LarkCardAction> immutableActions(List<LarkCardAction> values) {
        if (values == null || values.isEmpty()) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(new ArrayList<>(values));
    }

    public static final class Builder {
        private final String title;
        private final String summary;
        private final Map<String, String> fields = new LinkedHashMap<>();
        private final List<LarkCardAction> actions = new ArrayList<>();

        private Builder(String title, String summary) {
            this.title = title;
            this.summary = summary;
        }

        public Builder field(String label, String value) {
            fields.put(label, value);
            return this;
        }

        public Builder action(String label, String url) {
            actions.add(new LarkCardAction(label, url));
            return this;
        }

        public LarkCardMessage build() {
            return new LarkCardMessage(title, summary, fields, actions);
        }
    }
}
