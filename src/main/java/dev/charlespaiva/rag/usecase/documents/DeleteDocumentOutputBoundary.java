package dev.charlespaiva.rag.usecase.documents;

@FunctionalInterface
public interface DeleteDocumentOutputBoundary {
    void present(DeleteDocumentResponseModel responseModel);
}
