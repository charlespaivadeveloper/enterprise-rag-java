package dev.charlespaiva.rag.usecase.ingest;

@FunctionalInterface
public interface IngestDocumentInputBoundary {
    void execute(IngestDocumentRequestModel requestModel, IngestDocumentOutputBoundary outputBoundary);
}
