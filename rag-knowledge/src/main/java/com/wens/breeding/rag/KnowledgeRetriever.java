package com.wens.breeding.rag;

import java.util.List;

public interface KnowledgeRetriever {
    List<KnowledgeSearchResult> search(KnowledgeSearchRequest request);
}
