package dev.charlespaiva.rag.usecase.documents;

import java.util.UUID;

public record DeleteDocumentRequestModel(String tenantId, UUID documentId) {
}
