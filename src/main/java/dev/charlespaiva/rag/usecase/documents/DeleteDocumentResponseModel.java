package dev.charlespaiva.rag.usecase.documents;

public record DeleteDocumentResponseModel(boolean found, int deletedChunks) {
}
