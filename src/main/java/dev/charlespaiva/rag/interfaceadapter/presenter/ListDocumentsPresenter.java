package dev.charlespaiva.rag.interfaceadapter.presenter;

import dev.charlespaiva.rag.usecase.documents.ListDocumentsOutputBoundary;
import dev.charlespaiva.rag.usecase.documents.ListDocumentsResponseModel;
import java.util.List;

public class ListDocumentsPresenter implements ListDocumentsOutputBoundary {

    private DocumentListViewModel viewModel;

    @Override
    public void present(ListDocumentsResponseModel responseModel) {
        List<DocumentListViewModel.DocumentView> views = responseModel.documents().stream()
            .map(d -> new DocumentListViewModel.DocumentView(
                d.documentId().toString(),
                d.sourceName(),
                d.chunkCount(),
                d.createdAt().toString()))
            .toList();
        viewModel = new DocumentListViewModel(
            views,
            responseModel.page(),
            responseModel.size(),
            responseModel.totalElements(),
            responseModel.totalPages());
    }

    public DocumentListViewModel getViewModel() {
        return viewModel;
    }
}
