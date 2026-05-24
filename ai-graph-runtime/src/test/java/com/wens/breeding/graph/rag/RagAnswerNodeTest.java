package com.wens.breeding.graph.rag;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.wens.breeding.graph.llm.StaticJsonLlmGateway;
import com.wens.breeding.rag.InMemoryKnowledgeBase;
import com.wens.breeding.rag.KnowledgeDocument;
import com.wens.breeding.rag.TextChunker;

class RagAnswerNodeTest {
    @Test
    void answersWithCitationsFromKnowledgeBase() {
        InMemoryKnowledgeBase knowledgeBase = new InMemoryKnowledgeBase(new TextChunker(180, 20));
        knowledgeBase.index(document(
                "DOC-001",
                "Weight Below Standard Handling",
                "When body weight is below standard, review feed intake, house temperature, and disease signs.",
                "manual"));
        RagAnswerNode node = new RagAnswerNode(
                knowledgeBase,
                new StaticJsonLlmGateway("{\"answer\":\"Review feed intake and temperature.\",\"citations\":[{\"documentId\":\"DOC-001\",\"chunkId\":\"DOC-001#chunk-0\"}]}"));

        RagAnswer answer = node.answer(new RagAnswerRequest("body weight below standard", 2, "manual"));

        assertTrue(answer.getAnswer().contains("Review feed intake"));
        assertTrue(answer.hasCitations());
        assertEquals("DOC-001", answer.getCitations().get(0).getDocumentId());
        assertEquals("static-json-test-model", answer.getModelName());
    }

    @Test
    void returnsFallbackWhenKnowledgeIsMissing() {
        RagAnswerNode node = new RagAnswerNode(
                new InMemoryKnowledgeBase(new TextChunker(180, 20)),
                new StaticJsonLlmGateway("{\"answer\":\"unused\"}"));

        RagAnswer answer = node.answer(new RagAnswerRequest("unknown disease handling", 2, ""));

        assertFalse(answer.hasCitations());
        assertTrue(answer.getAnswer().contains("No matching knowledge"));
    }

    @Test
    void rejectsInvalidRequest() {
        assertThrows(IllegalArgumentException.class, () -> new RagAnswerRequest(" ", 1, ""));
        assertThrows(IllegalArgumentException.class, () -> new RagAnswerRequest("question", 0, ""));
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
