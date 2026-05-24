package com.wens.breeding.graph.rag;

public final class RagAnswerRequest {
    private final String question;
    private final int topK;
    private final String tagFilter;

    public RagAnswerRequest(String question) {
        this(question, 3, "");
    }

    public RagAnswerRequest(String question, int topK, String tagFilter) {
        if (topK <= 0) {
            throw new IllegalArgumentException("topK must be positive");
        }
        this.question = requireText(question, "question");
        this.topK = topK;
        this.tagFilter = tagFilter == null ? "" : tagFilter.trim();
    }

    public String getQuestion() {
        return question;
    }

    public int getTopK() {
        return topK;
    }

    public String getTagFilter() {
        return tagFilter;
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value;
    }
}
