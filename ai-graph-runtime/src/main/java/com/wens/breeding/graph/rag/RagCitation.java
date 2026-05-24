package com.wens.breeding.graph.rag;

public final class RagCitation {
    private final String documentId;
    private final String chunkId;
    private final String title;
    private final double score;
    private final String source;

    public RagCitation(String documentId, String chunkId, String title, double score, String source) {
        if (score < 0) {
            throw new IllegalArgumentException("score must be non-negative");
        }
        this.documentId = requireText(documentId, "documentId");
        this.chunkId = requireText(chunkId, "chunkId");
        this.title = requireText(title, "title");
        this.score = score;
        this.source = source == null ? "" : source;
    }

    public String getDocumentId() {
        return documentId;
    }

    public String getChunkId() {
        return chunkId;
    }

    public String getTitle() {
        return title;
    }

    public double getScore() {
        return score;
    }

    public String getSource() {
        return source;
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value;
    }
}
