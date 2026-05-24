package com.wens.breeding.rag;

public final class KnowledgeSearchRequest {
    private final String query;
    private final int topK;
    private final String tagFilter;

    public KnowledgeSearchRequest(String query, int topK) {
        this(query, topK, "");
    }

    public KnowledgeSearchRequest(String query, int topK, String tagFilter) {
        if (topK <= 0) {
            throw new IllegalArgumentException("topK must be positive");
        }
        this.query = requireText(query, "query");
        this.topK = topK;
        this.tagFilter = tagFilter == null ? "" : tagFilter.trim();
    }

    public String getQuery() {
        return query;
    }

    public int getTopK() {
        return topK;
    }

    public String getTagFilter() {
        return tagFilter;
    }

    public boolean hasTagFilter() {
        return !tagFilter.isEmpty();
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value;
    }
}
