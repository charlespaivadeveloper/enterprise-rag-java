package dev.charlespaiva.rag.usecase.ingest;

import java.util.UUID;

public record IngestDocumentResponseModel(UUID documentId, int chunks) {
}
