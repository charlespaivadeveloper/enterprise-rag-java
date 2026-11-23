package dev.charlespaiva.rag.usecase.documents;

public record ListDocumentsRequestModel(String tenantId, int page, int size) {
}
