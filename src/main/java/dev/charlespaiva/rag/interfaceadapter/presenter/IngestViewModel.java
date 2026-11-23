package dev.charlespaiva.rag.interfaceadapter.presenter;

import io.swagger.v3.oas.annotations.media.Schema;

public record IngestViewModel(
    @Schema(description = "Generated document identifier") String documentId,
    @Schema(description = "Number of chunks stored") int chunks,
    @Schema(description = "Object storage key (null for plain-text ingest)") String objectKey) {
}
