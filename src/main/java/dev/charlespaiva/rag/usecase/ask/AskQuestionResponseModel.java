package dev.charlespaiva.rag.usecase.ask;

import java.util.List;

public record AskQuestionResponseModel(String text, List<CitationData> citations, String traceId) {
    public AskQuestionResponseModel {
        citations = List.copyOf(citations);
    }

    public record CitationData(String source, int chunk, String excerpt, double score) {
    }
}
