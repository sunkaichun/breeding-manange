package com.wens.breeding.rag;

public final class KnowledgeSearchResult {
    private final KnowledgeChunk chunk;
    private final double score;

    public KnowledgeSearchResult(KnowledgeChunk chunk, double score) {
        if (chunk == null) {
            throw new IllegalArgumentException("chunk must not be null");
        }
        if (score < 0) {
            throw new IllegalArgumentException("score must be non-negative");
        }
        this.chunk = chunk;
        this.score = score;
    }

    public KnowledgeChunk getChunk() {
        return chunk;
    }

    public double getScore() {
        return score;
    }
}
