package dev.charlespaiva.rag.usecase.documents;

import dev.charlespaiva.rag.usecase.dataaccess.ChunkDataAccessInterface;
import dev.charlespaiva.rag.usecase.dataaccess.PagedResult;
import dev.charlespaiva.rag.usecase.dataaccess.DocumentSummaryData;
import java.util.List;

public class ListDocumentsInteractor implements ListDocumentsInputBoundary {

    private static final int MAX_PAGE_SIZE = 100;

    private final ChunkDataAccessInterface chunkDataAccess;

    public ListDocumentsInteractor(ChunkDataAccessInterface chunkDataAccess) {
        this.chunkDataAccess = chunkDataAccess;
    }

    @Override
    public void execute(ListDocumentsRequestModel request, ListDocumentsOutputBoundary outputBoundary) {
        if (request.tenantId() == null || request.tenantId().isBlank()) {
            throw new IllegalArgumentException("tenantId is required");
        }
        if (request.page() < 0) {
            throw new IllegalArgumentException("page must be >= 0");
        }
        if (request.size() < 1 || request.size() > MAX_PAGE_SIZE) {
            throw new IllegalArgumentException("size must be between 1 and " + MAX_PAGE_SIZE);
        }
        PagedResult<DocumentSummaryData> result =
            chunkDataAccess.listDocuments(request.tenantId(), request.page(), request.size());
        List<ListDocumentsResponseModel.DocumentData> docs = result.items().stream()
            .map(d -> new ListDocumentsResponseModel.DocumentData(
                d.documentId(), d.sourceName(), d.chunkCount(), d.createdAt()))
            .toList();
        outputBoundary.present(new ListDocumentsResponseModel(
            docs, result.totalElements(), request.page(), request.size()));
    }
}
