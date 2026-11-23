package dev.charlespaiva.rag.usecase.ask;

import dev.charlespaiva.rag.usecase.common.PromptInjectionGuard;
import dev.charlespaiva.rag.usecase.common.RetrievedChunk;
import dev.charlespaiva.rag.usecase.common.RrfFuser;
import dev.charlespaiva.rag.usecase.dataaccess.ChunkDataAccessInterface;
import dev.charlespaiva.rag.usecase.dataaccess.EmbeddingServiceInterface;
import dev.charlespaiva.rag.usecase.dataaccess.GenerationServiceInterface;
import dev.charlespaiva.rag.usecase.dataaccess.RerankingServiceInterface;
import java.util.List;
import java.util.UUID;

public class AskQuestionInteractor implements AskQuestionInputBoundary {

    private final ChunkDataAccessInterface chunkDataAccess;
    private final EmbeddingServiceInterface embeddingService;
    private final GenerationServiceInterface generationService;
    private final PromptInjectionGuard guard;
    private final RrfFuser rrfFuser;
    private final RerankingServiceInterface rerankingService;

    public AskQuestionInteractor(
            ChunkDataAccessInterface chunkDataAccess,
            EmbeddingServiceInterface embeddingService,
            GenerationServiceInterface generationService,
            PromptInjectionGuard guard,
            RrfFuser rrfFuser,
            RerankingServiceInterface rerankingService) {
        this.chunkDataAccess = chunkDataAccess;
        this.embeddingService = embeddingService;
        this.generationService = generationService;
        this.guard = guard;
        this.rrfFuser = rrfFuser;
        this.rerankingService = rerankingService;
    }

    @Override
    public void execute(AskQuestionRequestModel requestModel, AskQuestionOutputBoundary outputBoundary) {
        guard.validateQuery(requestModel.question());
        int topK = Math.min(Math.max(requestModel.topK(), 1), 20);
        int fetchWindow = topK * 2;
        float[] embedding = embeddingService.embed(requestModel.question());
        List<RetrievedChunk> semantic = chunkDataAccess.semanticSearch(requestModel.tenantId(), embedding, fetchWindow);
        List<RetrievedChunk> lexical = chunkDataAccess.lexicalSearch(requestModel.tenantId(), requestModel.question(), fetchWindow);
        List<RetrievedChunk> fused = rrfFuser.fuse(semantic, lexical).stream().limit(topK).toList();
        List<RetrievedChunk> evidence = rerankingService.rerank(requestModel.question(), fused);
        String text = evidence.isEmpty()
            ? "Não encontrei evidências suficientes na base autorizada."
            : generationService.generate(requestModel.question(), evidence);
        List<AskQuestionResponseModel.CitationData> citations = evidence.stream()
            .map(e -> new AskQuestionResponseModel.CitationData(
                e.chunk().sourceName(), e.chunk().ordinal(),
                e.chunk().content().substring(0, Math.min(220, e.chunk().content().length())),
                e.finalScore()))
            .toList();
        outputBoundary.present(new AskQuestionResponseModel(text, citations, UUID.randomUUID().toString()));
    }
}
