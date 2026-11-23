package dev.charlespaiva.rag.usecase.ingest;

import dev.charlespaiva.rag.entity.KnowledgeChunk;
import dev.charlespaiva.rag.usecase.common.DocumentChunker;
import dev.charlespaiva.rag.usecase.common.PromptInjectionGuard;
import dev.charlespaiva.rag.usecase.dataaccess.ChunkDataAccessInterface;
import dev.charlespaiva.rag.usecase.dataaccess.EmbeddingServiceInterface;

import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

public class IngestDocumentInteractor implements IngestDocumentInputBoundary {

    private final ChunkDataAccessInterface chunkDataAccess;
    private final EmbeddingServiceInterface embeddingService;
    private final DocumentChunker chunker;
    private final PromptInjectionGuard guard;
    private final PiiRedactorPort piiRedactor;

    public IngestDocumentInteractor(
            ChunkDataAccessInterface chunkDataAccess,
            EmbeddingServiceInterface embeddingService,
            DocumentChunker chunker,
            PromptInjectionGuard guard,
            PiiRedactorPort piiRedactor) {
        this.chunkDataAccess = chunkDataAccess;
        this.embeddingService = embeddingService;
        this.chunker = chunker;
        this.guard = guard;
        this.piiRedactor = piiRedactor;
    }

    @Override
    public void execute(IngestDocumentRequestModel requestModel, IngestDocumentOutputBoundary outputBoundary) {
        UUID documentId = UUID.randomUUID();
        String sanitized = piiRedactor.redact(guard.sanitizeDocument(requestModel.content()));
        List<String> parts = chunker.split(sanitized, 900, 120);
        List<KnowledgeChunk> chunks = IntStream.range(0, parts.size())
            .mapToObj(i -> new KnowledgeChunk(UUID.randomUUID(), requestModel.tenantId(), documentId, i, parts.get(i), requestModel.sourceName()))
            .toList();
        chunkDataAccess.saveAll(chunks, embeddingService.embed(parts));
        outputBoundary.present(new IngestDocumentResponseModel(documentId, chunks.size()));
    }
}
