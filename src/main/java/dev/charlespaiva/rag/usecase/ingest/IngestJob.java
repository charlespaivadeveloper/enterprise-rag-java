package dev.charlespaiva.rag.usecase.ingest;

import java.time.Instant;
import java.util.UUID;

public record IngestJob(
    UUID jobId,
    String tenantId,
    String sourceName,
    IngestJobStatus status,
    Instant createdAt,
    String failureReason
) {
}
