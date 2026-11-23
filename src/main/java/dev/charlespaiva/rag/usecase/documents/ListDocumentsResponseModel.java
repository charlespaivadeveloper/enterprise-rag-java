package dev.charlespaiva.rag.usecase.documents;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ListDocumentsResponseModel(
        List<DocumentData> documents,
        long totalElements,
        int page,
        int size) {

    public ListDocumentsResponseModel {
        documents = List.copyOf(documents);
    }

    public int totalPages() {
        return size == 0 ? 0 : (int) Math.ceil((double) totalElements / size);
    }

    public record DocumentData(UUID documentId, String sourceName, int chunkCount, Instant createdAt) {
    }
}
