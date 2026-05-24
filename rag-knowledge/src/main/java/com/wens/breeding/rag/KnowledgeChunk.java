package com.wens.breeding.rag;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class KnowledgeChunk {
    private final String chunkId;
    private final String documentId;
    private final String title;
    private final String content;
    private final int chunkIndex;
    private final Map<String, String> metadata;

    public KnowledgeChunk(
            String chunkId,
            String documentId,
            String title,
            String content,
            int chunkIndex,
            Map<String, String> metadata) {
        if (chunkIndex < 0) {
            throw new IllegalArgumentException("chunkIndex must be non-negative");
        }
        this.chunkId = requireText(chunkId, "chunkId");
        this.documentId = requireText(documentId, "documentId");
        this.title = requireText(title, "title");
        this.content = requireText(content, "content");
        this.chunkIndex = chunkIndex;
        this.metadata = metadata == null
                ? Collections.emptyMap()
                : Collections.unmodifiableMap(new LinkedHashMap<>(metadata));
    }

    public String getChunkId() {
        return chunkId;
    }

    public String getDocumentId() {
        return documentId;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public int getChunkIndex() {
        return chunkIndex;
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
