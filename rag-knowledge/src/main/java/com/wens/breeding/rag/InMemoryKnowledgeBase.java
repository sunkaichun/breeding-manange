package com.wens.breeding.rag;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public final class InMemoryKnowledgeBase implements KnowledgeRetriever {
    private final TextChunker chunker;
    private final Map<String, KnowledgeDocument> documents = new LinkedHashMap<>();
    private final List<KnowledgeChunk> chunks = new ArrayList<>();

    public InMemoryKnowledgeBase(TextChunker chunker) {
        if (chunker == null) {
            throw new IllegalArgumentException("chunker must not be null");
        }
        this.chunker = chunker;
    }

    public void index(KnowledgeDocument document) {
        if (document == null) {
            throw new IllegalArgumentException("document must not be null");
        }
        documents.put(document.getDocumentId(), document);
        chunks.removeIf(chunk -> document.getDocumentId().equals(chunk.getDocumentId()));
        chunks.addAll(chunker.chunk(document));
    }

    public int documentCount() {
        return documents.size();
    }

    public int chunkCount() {
        return chunks.size();
    }

    @Override
    public List<KnowledgeSearchResult> search(KnowledgeSearchRequest request) {
        Set<String> queryTerms = tokenize(request.getQuery());
        return chunks.stream()
                .filter(chunk -> matchesTagFilter(chunk, request))
                .map(chunk -> new KnowledgeSearchResult(chunk, score(chunk, queryTerms)))
                .filter(result -> result.getScore() > 0)
                .sorted(Comparator
                        .comparingDouble(KnowledgeSearchResult::getScore)
                        .reversed()
                        .thenComparing(result -> result.getChunk().getDocumentId())
                        .thenComparing(result -> result.getChunk().getChunkIndex()))
                .limit(request.getTopK())
                .collect(Collectors.toList());
    }

    private static boolean matchesTagFilter(KnowledgeChunk chunk, KnowledgeSearchRequest request) {
        if (!request.hasTagFilter()) {
            return true;
        }
        String tags = chunk.getMetadata().getOrDefault("tags", "");
        for (String tag : tags.split(",")) {
            if (request.getTagFilter().equalsIgnoreCase(tag.trim())) {
                return true;
            }
        }
        return false;
    }

    private static double score(KnowledgeChunk chunk, Set<String> queryTerms) {
        String title = normalize(chunk.getTitle());
        String content = normalize(chunk.getContent());
        double score = 0;
        for (String term : queryTerms) {
            if (title.contains(term)) {
                score += 2.0;
            }
            score += countOccurrences(content, term);
        }
        return score;
    }

    private static int countOccurrences(String text, String term) {
        int count = 0;
        int fromIndex = 0;
        while (fromIndex < text.length()) {
            int index = text.indexOf(term, fromIndex);
            if (index < 0) {
                break;
            }
            count++;
            fromIndex = index + term.length();
        }
        return count;
    }

    private static Set<String> tokenize(String text) {
        String normalized = normalize(text);
        Set<String> terms = new LinkedHashSet<>();
        for (String token : normalized.split("[^\\p{IsAlphabetic}\\p{IsDigit}\\p{IsHan}]+")) {
            if (token.length() >= 2) {
                terms.add(token);
            }
        }
        if (terms.isEmpty() && !normalized.isEmpty()) {
            terms.add(normalized);
        }
        return terms;
    }

    private static String normalize(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT).trim();
    }
}
