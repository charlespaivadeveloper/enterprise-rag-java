package dev.charlespaiva.rag.usecase.ingest;

import java.time.Instant;
import java.util.UUID;

public class SubmitIngestJobInteractor implements SubmitIngestJobInputBoundary {

    private final IngestJobRepositoryPort jobRepo;
    private final IngestJobDispatcherPort dispatcher;

    public SubmitIngestJobInteractor(IngestJobRepositoryPort jobRepo, IngestJobDispatcherPort dispatcher) {
        this.jobRepo = jobRepo;
        this.dispatcher = dispatcher;
    }

    @Override
    public void execute(IngestDocumentRequestModel request, SubmitIngestJobOutputBoundary output) {
        UUID jobId = UUID.randomUUID();
        IngestJob job = new IngestJob(jobId, request.tenantId(), request.sourceName(),
            IngestJobStatus.PENDING, Instant.now(), null);
        jobRepo.save(job);
        dispatcher.dispatch(jobId, request.tenantId(), request.sourceName(), request.content());
        output.present(new SubmitIngestJobResponseModel(jobId, IngestJobStatus.PENDING));
    }
}
