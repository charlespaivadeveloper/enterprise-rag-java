package dev.charlespaiva.rag.interfaceadapter.presenter;

import dev.charlespaiva.rag.usecase.documents.DeleteDocumentOutputBoundary;
import dev.charlespaiva.rag.usecase.documents.DeleteDocumentResponseModel;

public class DeleteDocumentPresenter implements DeleteDocumentOutputBoundary {

    private boolean found;

    @Override
    public void present(DeleteDocumentResponseModel responseModel) {
        found = responseModel.found();
    }

    public boolean wasFound() {
        return found;
    }
}
