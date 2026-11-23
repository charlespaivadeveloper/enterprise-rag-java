package dev.charlespaiva.rag.usecase.ingest;

import java.util.UUID;

@FunctionalInterface
public interface GetIngestJobStatusInputBoundary {
    void execute(UUID jobId, String tenantId, GetIngestJobStatusOutputBoundary output);
}
