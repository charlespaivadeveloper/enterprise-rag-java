package dev.charlespaiva.rag.usecase.documents;

import dev.charlespaiva.rag.usecase.dataaccess.ChunkDataAccessInterface;

public class DeleteDocumentInteractor implements DeleteDocumentInputBoundary {

    private final ChunkDataAccessInterface chunkDataAccess;

    public DeleteDocumentInteractor(ChunkDataAccessInterface chunkDataAccess) {
        this.chunkDataAccess = chunkDataAccess;
    }

    @Override
    public void execute(DeleteDocumentRequestModel request, DeleteDocumentOutputBoundary outputBoundary) {
        if (request.tenantId() == null || request.tenantId().isBlank()) {
            throw new IllegalArgumentException("tenantId is required");
        }
        if (request.documentId() == null) {
            throw new IllegalArgumentException("documentId is required");
        }
        int deleted = chunkDataAccess.deleteByDocument(request.tenantId(), request.documentId());
        outputBoundary.present(new DeleteDocumentResponseModel(deleted > 0, deleted));
    }
}
