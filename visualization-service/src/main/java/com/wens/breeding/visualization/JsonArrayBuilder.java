package com.wens.breeding.visualization;

import java.math.BigDecimal;

final class JsonArrayBuilder {
    private final StringBuilder builder = new StringBuilder("[");
    private boolean firstObject = true;
    private boolean firstField = true;
    private boolean objectOpen = false;

    JsonArrayBuilder object() {
        if (objectOpen) {
            throw new IllegalStateException("Previous object is still open");
        }
        if (!firstObject) {
            builder.append(',');
        }
        builder.append('{');
        firstObject = false;
        firstField = true;
        objectOpen = true;
        return this;
    }

    JsonArrayBuilder string(String key, String value) {
        fieldPrefix(key);
        builder.append('"').append(escape(value)).append('"');
        return this;
    }

    JsonArrayBuilder number(String key, int value) {
        fieldPrefix(key);
        builder.append(value);
        return this;
    }

    JsonArrayBuilder number(String key, BigDecimal value) {
        fieldPrefix(key);
        builder.append(value.stripTrailingZeros().toPlainString());
        return this;
    }

    JsonArrayBuilder endObject() {
        if (!objectOpen) {
            throw new IllegalStateException("No open object to end");
        }
        builder.append('}');
        objectOpen = false;
        return this;
    }

    String build() {
        if (objectOpen) {
            throw new IllegalStateException("Cannot build JSON while an object is open");
        }
        return builder.append(']').toString();
    }

    private void fieldPrefix(String key) {
        if (!objectOpen) {
            throw new IllegalStateException("No open object for field");
        }
        if (!firstField) {
            builder.append(',');
        }
        builder.append('"').append(escape(key)).append('"').append(':');
        firstField = false;
    }

    private static String escape(String value) {
        if (value == null) {
            return "";
        }
        StringBuilder escaped = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            switch (ch) {
                case '"':
                    escaped.append("\\\"");
                    break;
                case '\\':
                    escaped.append("\\\\");
                    break;
                case '\n':
                    escaped.append("\\n");
                    break;
                case '\r':
                    escaped.append("\\r");
                    break;
                case '\t':
                    escaped.append("\\t");
                    break;
                default:
                    escaped.append(ch);
                    break;
            }
        }
        return escaped.toString();
    }
}
