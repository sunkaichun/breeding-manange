package com.wens.breeding.app.agent;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class BatchIdExtractor {
    private static final Pattern BATCH_ID_PATTERN = Pattern.compile("\\bBATCH[-_][A-Za-z0-9_-]+\\b", Pattern.CASE_INSENSITIVE);

    private BatchIdExtractor() {
    }

    static Optional<String> extract(String text) {
        if (text == null) {
            return Optional.empty();
        }
        Matcher matcher = BATCH_ID_PATTERN.matcher(text);
        if (matcher.find()) {
            return Optional.of(matcher.group().toUpperCase());
        }
        return Optional.empty();
    }
}
