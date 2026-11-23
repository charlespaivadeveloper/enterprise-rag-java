package dev.charlespaiva.rag.usecase.documents;

@FunctionalInterface
public interface ListDocumentsOutputBoundary {
    void present(ListDocumentsResponseModel responseModel);
}
