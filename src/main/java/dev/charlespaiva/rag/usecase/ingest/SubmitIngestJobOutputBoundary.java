package dev.charlespaiva.rag.usecase.ingest;

@FunctionalInterface
public interface SubmitIngestJobOutputBoundary {
    void present(SubmitIngestJobResponseModel response);
}
