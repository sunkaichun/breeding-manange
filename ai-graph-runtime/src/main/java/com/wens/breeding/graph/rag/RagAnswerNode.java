package com.wens.breeding.graph.rag;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.wens.breeding.graph.llm.LlmGateway;
import com.wens.breeding.graph.llm.LlmRequest;
import com.wens.breeding.graph.llm.LlmResponse;
import com.wens.breeding.rag.KnowledgeChunk;
import com.wens.breeding.rag.KnowledgeRetriever;
import com.wens.breeding.rag.KnowledgeSearchRequest;
import com.wens.breeding.rag.KnowledgeSearchResult;

public final class RagAnswerNode {
    private static final String DEFAULT_SCHEMA = "{"
            + "\"answer\":\"string\","
            + "\"citations\":[{\"documentId\":\"string\",\"chunkId\":\"string\"}]"
            + "}";

    private final KnowledgeRetriever knowledgeRetriever;
    private final LlmGateway llmGateway;
    private final int defaultTopK;

    public RagAnswerNode(KnowledgeRetriever knowledgeRetriever, LlmGateway llmGateway) {
        this(knowledgeRetriever, llmGateway, 3);
    }

    public RagAnswerNode(KnowledgeRetriever knowledgeRetriever, LlmGateway llmGateway, int defaultTopK) {
        if (defaultTopK <= 0) {
            throw new IllegalArgumentException("defaultTopK must be positive");
        }
        this.knowledgeRetriever = Objects.requireNonNull(knowledgeRetriever, "knowledgeRetriever");
        this.llmGateway = Objects.requireNonNull(llmGateway, "llmGateway");
        this.defaultTopK = defaultTopK;
    }

    public RagAnswer answer(RagAnswerRequest request) {
        Objects.requireNonNull(request, "request");
        int topK = request.getTopK() > 0 ? request.getTopK() : defaultTopK;
        KnowledgeSearchRequest searchRequest = new KnowledgeSearchRequest(
                request.getQuestion(),
                topK,
                request.getTagFilter());
        List<KnowledgeSearchResult> searchResults = knowledgeRetriever.search(searchRequest);
        List<RagCitation> citations = toCitations(searchResults);

        if (searchResults.isEmpty()) {
            return new RagAnswer(
                    request.getQuestion(),
                    "No matching knowledge was found. Please add or update the knowledge base before answering this question.",
                    citations,
                    "");
        }

        LlmResponse response = llmGateway.complete(LlmRequest.builder()
                .systemPrompt(systemPrompt())
                .userPrompt(buildUserPrompt(request.getQuestion(), searchResults))
                .responseSchema(DEFAULT_SCHEMA)
                .metadata("requestType", "rag-answer")
                .build());

        return new RagAnswer(
                request.getQuestion(),
                response.getContent(),
                citations,
                response.getModelName());
    }

    private static String systemPrompt() {
        return "You answer breeding management questions using only the provided knowledge snippets. "
                + "Always keep the answer operational and include the cited document/chunk identifiers.";
    }

    private static String buildUserPrompt(String question, List<KnowledgeSearchResult> searchResults) {
        StringBuilder builder = new StringBuilder();
        builder.append("Question:\n").append(question).append("\n\n");
        builder.append("Knowledge snippets:\n");
        for (int i = 0; i < searchResults.size(); i++) {
            KnowledgeSearchResult result = searchResults.get(i);
            KnowledgeChunk chunk = result.getChunk();
            builder.append("[")
                    .append(i + 1)
                    .append("] documentId=")
                    .append(chunk.getDocumentId())
                    .append(", chunkId=")
                    .append(chunk.getChunkId())
                    .append(", title=")
                    .append(chunk.getTitle())
                    .append(", score=")
                    .append(result.getScore())
                    .append("\n")
                    .append(chunk.getContent())
                    .append("\n\n");
        }
        builder.append("Return a JSON object following the response schema.");
        return builder.toString();
    }

    private static List<RagCitation> toCitations(List<KnowledgeSearchResult> searchResults) {
        List<RagCitation> citations = new ArrayList<>();
        for (KnowledgeSearchResult result : searchResults) {
            KnowledgeChunk chunk = result.getChunk();
            citations.add(new RagCitation(
                    chunk.getDocumentId(),
                    chunk.getChunkId(),
                    chunk.getTitle(),
                    result.getScore(),
                    chunk.getMetadata().getOrDefault("source", "")));
        }
        return citations;
    }
}
