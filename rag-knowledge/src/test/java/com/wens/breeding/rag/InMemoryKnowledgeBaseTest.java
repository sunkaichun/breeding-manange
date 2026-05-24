package com.wens.breeding.rag;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

class InMemoryKnowledgeBaseTest {
    @Test
    void chunksDocumentByConfiguredSize() {
        KnowledgeDocument document = document(
                "DOC-001",
                "Brooding Manual",
                "First paragraph about temperature control.\n\nSecond paragraph about ventilation.");

        List<KnowledgeChunk> chunks = new TextChunker(45, 5).chunk(document);

        assertEquals(2, chunks.size());
        assertEquals("DOC-001#chunk-0", chunks.get(0).getChunkId());
        assertEquals("manual", chunks.get(0).getMetadata().get("tags"));
    }

    @Test
    void retrievesRelevantChunksByKeywordScore() {
        InMemoryKnowledgeBase knowledgeBase = new InMemoryKnowledgeBase(new TextChunker(120, 10));
        knowledgeBase.index(document(
                "DOC-001",
                "Weight Management",
                "When body weight is below standard, review feed intake, temperature, and disease signs."));
        knowledgeBase.index(document(
                "DOC-002",
                "FCR Management",
                "When FCR is high, review feed waste, mortality, and formula execution."));

        List<KnowledgeSearchResult> results = knowledgeBase.search(new KnowledgeSearchRequest("high FCR feed waste", 2));

        assertEquals(2, knowledgeBase.documentCount());
        assertEquals(2, knowledgeBase.chunkCount());
        assertEquals("DOC-002", results.get(0).getChunk().getDocumentId());
        assertTrue(results.get(0).getScore() > 0);
    }

    @Test
    void filtersByTag() {
        InMemoryKnowledgeBase knowledgeBase = new InMemoryKnowledgeBase(new TextChunker(120, 10));
        knowledgeBase.index(document(
                "DOC-001",
                "Weight Management",
                "Weight below standard requires feed review.",
                "manual"));
        knowledgeBase.index(document(
                "DOC-002",
                "Medicine Manual",
                "Weight and disease signs require veterinary review.",
                "medicine"));

        List<KnowledgeSearchResult> results = knowledgeBase.search(new KnowledgeSearchRequest("weight review", 5, "medicine"));

        assertEquals(1, results.size());
        assertEquals("DOC-002", results.get(0).getChunk().getDocumentId());
    }

    @Test
    void rejectsInvalidChunkerConfig() {
        assertThrows(IllegalArgumentException.class, () -> new TextChunker(10, 10));
    }

    private static KnowledgeDocument document(String documentId, String title, String content) {
        return document(documentId, title, content, "manual");
    }

    private static KnowledgeDocument document(String documentId, String title, String content, String tags) {
        Map<String, String> metadata = new LinkedHashMap<>();
        metadata.put("tags", tags);
        return new KnowledgeDocument(
                documentId,
                title,
                "open-source-knowledge",
                "v1",
                content,
                Instant.parse("2026-05-24T00:00:00Z"),
                Collections.unmodifiableMap(metadata));
    }
}
