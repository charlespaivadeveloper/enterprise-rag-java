package dev.charlespaiva.rag.usecase.ingest;

import java.util.UUID;

public class GetIngestJobStatusInteractor implements GetIngestJobStatusInputBoundary {

    private final IngestJobRepositoryPort jobRepo;

    public GetIngestJobStatusInteractor(IngestJobRepositoryPort jobRepo) {
        this.jobRepo = jobRepo;
    }

    @Override
    public void execute(UUID jobId, String tenantId, GetIngestJobStatusOutputBoundary output) {
        jobRepo.findById(jobId, tenantId)
            .map(job -> new GetIngestJobStatusResponseModel(job.jobId(), job.tenantId(), job.sourceName(),
                job.status(), job.createdAt(), job.failureReason()))
            .ifPresentOrElse(output::present, output::notFound);
    }
}
