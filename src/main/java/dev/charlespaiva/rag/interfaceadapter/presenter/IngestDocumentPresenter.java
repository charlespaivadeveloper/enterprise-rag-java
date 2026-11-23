package dev.charlespaiva.rag.interfaceadapter.presenter;

import dev.charlespaiva.rag.usecase.ingest.IngestDocumentOutputBoundary;
import dev.charlespaiva.rag.usecase.ingest.IngestDocumentResponseModel;

public class IngestDocumentPresenter implements IngestDocumentOutputBoundary {

    private IngestViewModel viewModel;

    @Override
    public void present(IngestDocumentResponseModel responseModel) {
        viewModel = new IngestViewModel(responseModel.documentId().toString(), responseModel.chunks(), null);
    }

    public IngestViewModel getViewModel() {
        return viewModel;
    }
}
