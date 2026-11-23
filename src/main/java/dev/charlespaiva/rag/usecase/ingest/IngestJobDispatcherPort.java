package dev.charlespaiva.rag.usecase.ingest;

import java.util.UUID;

@FunctionalInterface
public interface IngestJobDispatcherPort {
    void dispatch(UUID jobId, String tenantId, String sourceName, String content);
}
