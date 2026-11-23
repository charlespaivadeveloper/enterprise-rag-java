package dev.charlespaiva.rag.usecase.ingest;

import java.util.Optional;
import java.util.UUID;

public interface IngestJobRepositoryPort {
    void save(IngestJob job);
    Optional<IngestJob> findById(UUID jobId, String tenantId);
    void updateStatus(UUID jobId, String tenantId, IngestJobStatus status, String failureReason);
}
