package dev.charlespaiva.rag.usecase.dataaccess;

import java.time.Instant;
import java.util.UUID;

public record DocumentSummaryData(UUID documentId, String sourceName, int chunkCount, Instant createdAt) {
}
