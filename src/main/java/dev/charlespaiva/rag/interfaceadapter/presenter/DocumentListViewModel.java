package dev.charlespaiva.rag.interfaceadapter.presenter;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record DocumentListViewModel(
    @Schema(description = "Documents on the current page") List<DocumentView> content,
    @Schema(description = "Current page number (0-based)") int page,
    @Schema(description = "Page size requested") int size,
    @Schema(description = "Total number of documents across all pages") long totalElements,
    @Schema(description = "Total number of pages") int totalPages) {

    public DocumentListViewModel {
        content = List.copyOf(content);
    }

    public record DocumentView(
        @Schema(description = "Document UUID") String documentId,
        @Schema(description = "Source file name") String sourceName,
        @Schema(description = "Number of stored chunks") int chunkCount,
        @Schema(description = "ISO-8601 ingestion timestamp") String createdAt) {
    }
}
