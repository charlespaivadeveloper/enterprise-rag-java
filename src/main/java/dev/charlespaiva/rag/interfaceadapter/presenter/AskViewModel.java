package dev.charlespaiva.rag.interfaceadapter.presenter;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record AskViewModel(
    @Schema(description = "Generated answer grounded in retrieved evidence") String answer,
    @Schema(description = "Source citations supporting the answer") List<CitationView> citations,
    @Schema(description = "Trace identifier for observability") String traceId) {

    public AskViewModel {
        citations = List.copyOf(citations);
    }

    public record CitationView(
        @Schema(description = "Source document name") String source,
        @Schema(description = "Chunk ordinal within the document") int chunk,
        @Schema(description = "Short excerpt from the chunk") String excerpt,
        @Schema(description = "Hybrid retrieval score") double score) {
    }
}
