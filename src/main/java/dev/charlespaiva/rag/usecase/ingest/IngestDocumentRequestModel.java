package dev.charlespaiva.rag.usecase.ingest;

public record IngestDocumentRequestModel(String tenantId, String sourceName, String content) {
}
