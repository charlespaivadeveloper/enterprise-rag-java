package dev.charlespaiva.rag.interfaceadapter.gateway;

import dev.charlespaiva.rag.interfaceadapter.presenter.IngestDocumentPresenter;
import dev.charlespaiva.rag.usecase.ingest.IngestDocumentInputBoundary;
import dev.charlespaiva.rag.usecase.ingest.IngestDocumentRequestModel;
import dev.charlespaiva.rag.usecase.ingest.IngestJobDispatcherPort;
import dev.charlespaiva.rag.usecase.ingest.IngestJobRepositoryPort;
import dev.charlespaiva.rag.usecase.ingest.IngestJobStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import java.util.UUID;

@Component
public class SpringAsyncIngestJobDispatcher implements IngestJobDispatcherPort {

    private final IngestDocumentInputBoundary ingestBoundary;
    private final IngestJobRepositoryPort jobRepo;

    public SpringAsyncIngestJobDispatcher(IngestDocumentInputBoundary ingestBoundary,
                                           IngestJobRepositoryPort jobRepo) {
        this.ingestBoundary = ingestBoundary;
        this.jobRepo = jobRepo;
    }

    @Override
    @Async("ingestExecutor")
    public void dispatch(UUID jobId, String tenantId, String sourceName, String content) {
        jobRepo.updateStatus(jobId, tenantId, IngestJobStatus.PROCESSING, null);
        try {
            IngestDocumentPresenter presenter = new IngestDocumentPresenter();
            ingestBoundary.execute(new IngestDocumentRequestModel(tenantId, sourceName, content), presenter);
            jobRepo.updateStatus(jobId, tenantId, IngestJobStatus.COMPLETED, null);
        } catch (Exception e) {
            jobRepo.updateStatus(jobId, tenantId, IngestJobStatus.FAILED, e.getMessage());
        }
    }
}
