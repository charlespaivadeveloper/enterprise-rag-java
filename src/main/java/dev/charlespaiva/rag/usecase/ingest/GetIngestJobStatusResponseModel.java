package dev.charlespaiva.rag.usecase.ingest;

import java.time.Instant;
import java.util.UUID;

public record GetIngestJobStatusResponseModel(
    UUID jobId,
    String tenantId,
    String sourceName,
    IngestJobStatus status,
    Instant createdAt,
    String failureReason
) {
}
