package dev.charlespaiva.rag.usecase.ingest;

import java.util.UUID;

public record SubmitIngestJobResponseModel(UUID jobId, IngestJobStatus status) {
}
