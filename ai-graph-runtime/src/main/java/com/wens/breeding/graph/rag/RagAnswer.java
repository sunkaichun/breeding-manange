package com.wens.breeding.graph.rag;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class RagAnswer {
    private final String question;
    private final String answer;
    private final List<RagCitation> citations;
    private final String modelName;

    public RagAnswer(String question, String answer, List<RagCitation> citations, String modelName) {
        this.question = requireText(question, "question");
        this.answer = requireText(answer, "answer");
        this.citations = citations == null
                ? Collections.emptyList()
                : Collections.unmodifiableList(new ArrayList<>(citations));
        this.modelName = modelName == null ? "" : modelName;
    }

    public String getQuestion() {
        return question;
    }

    public String getAnswer() {
        return answer;
    }

    public List<RagCitation> getCitations() {
        return citations;
    }

    public String getModelName() {
        return modelName;
    }

    public boolean hasCitations() {
        return !citations.isEmpty();
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value;
    }
}
