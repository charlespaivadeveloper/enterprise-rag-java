package dev.charlespaiva.rag.usecase.ingest;

public interface GetIngestJobStatusOutputBoundary {
    void present(GetIngestJobStatusResponseModel response);
    void notFound();
}
