package dev.charlespaiva.rag.usecase.ingest;

@FunctionalInterface
public interface SubmitIngestJobInputBoundary {
    void execute(IngestDocumentRequestModel request, SubmitIngestJobOutputBoundary output);
}
