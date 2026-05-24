package com.wens.breeding.rag;

import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public final class KnowledgeDocument {
    private final String documentId;
    private final String title;
    private final String source;
    private final String version;
    private final String content;
    private final Instant updatedAt;
    private final Map<String, String> metadata;

    public KnowledgeDocument(
            String documentId,
            String title,
            String source,
            String version,
            String content,
            Instant updatedAt,
            Map<String, String> metadata) {
        this.documentId = requireText(documentId, "documentId");
        this.title = requireText(title, "title");
        this.source = requireText(source, "source");
        this.version = version == null || version.trim().isEmpty() ? "default" : version;
        this.content = requireText(content, "content");
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt");
        this.metadata = metadata == null
                ? Collections.emptyMap()
                : Collections.unmodifiableMap(new LinkedHashMap<>(metadata));
    }

    public String getDocumentId() {
        return documentId;
    }

    public String getTitle() {
        return title;
    }

    public String getSource() {
        return source;
    }

    public String getVersion() {
        return version;
    }

    public String getContent() {
        return content;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value;
    }
}
