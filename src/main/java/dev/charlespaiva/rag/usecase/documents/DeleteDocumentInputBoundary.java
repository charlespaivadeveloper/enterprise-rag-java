package dev.charlespaiva.rag.usecase.documents;

@FunctionalInterface
public interface DeleteDocumentInputBoundary {
    void execute(DeleteDocumentRequestModel requestModel, DeleteDocumentOutputBoundary outputBoundary);
}
