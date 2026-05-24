package com.wens.breeding.rag;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class TextChunker {
    private final int maxChunkChars;
    private final int overlapChars;

    public TextChunker(int maxChunkChars, int overlapChars) {
        if (maxChunkChars <= 0) {
            throw new IllegalArgumentException("maxChunkChars must be positive");
        }
        if (overlapChars < 0 || overlapChars >= maxChunkChars) {
            throw new IllegalArgumentException("overlapChars must be non-negative and less than maxChunkChars");
        }
        this.maxChunkChars = maxChunkChars;
        this.overlapChars = overlapChars;
    }

    public List<KnowledgeChunk> chunk(KnowledgeDocument document) {
        String normalizedContent = document.getContent().replace("\r\n", "\n").trim();
        List<KnowledgeChunk> chunks = new ArrayList<>();
        int start = 0;
        int chunkIndex = 0;
        while (start < normalizedContent.length()) {
            int end = Math.min(normalizedContent.length(), start + maxChunkChars);
            if (end < normalizedContent.length()) {
                int paragraphBreak = normalizedContent.lastIndexOf("\n\n", end);
                if (paragraphBreak > start) {
                    end = paragraphBreak;
                }
            }
            String chunkContent = normalizedContent.substring(start, end).trim();
            if (!chunkContent.isEmpty()) {
                Map<String, String> metadata = new LinkedHashMap<>(document.getMetadata());
                metadata.put("source", document.getSource());
                metadata.put("version", document.getVersion());
                chunks.add(new KnowledgeChunk(
                        document.getDocumentId() + "#chunk-" + chunkIndex,
                        document.getDocumentId(),
                        document.getTitle(),
                        chunkContent,
                        chunkIndex,
                        metadata));
                chunkIndex++;
            }
            if (end == normalizedContent.length()) {
                break;
            }
            start = Math.max(0, end - overlapChars);
            while (start < normalizedContent.length() && Character.isWhitespace(normalizedContent.charAt(start))) {
                start++;
            }
        }
        return chunks;
    }
}
