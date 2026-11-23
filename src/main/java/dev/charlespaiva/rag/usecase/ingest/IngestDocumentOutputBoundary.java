package dev.charlespaiva.rag.usecase.ingest;

@FunctionalInterface
public interface IngestDocumentOutputBoundary {
    void present(IngestDocumentResponseModel responseModel);
}
