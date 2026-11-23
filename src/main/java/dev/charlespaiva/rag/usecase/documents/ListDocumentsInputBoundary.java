package dev.charlespaiva.rag.usecase.documents;

@FunctionalInterface
public interface ListDocumentsInputBoundary {
    void execute(ListDocumentsRequestModel requestModel, ListDocumentsOutputBoundary outputBoundary);
}
