package dev.charlespaiva.rag.usecase.common;

import dev.charlespaiva.rag.entity.KnowledgeChunk;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class RrfFuserTest {

    private final RrfFuser fuser = new RrfFuser();

    @Test
    void chunkInBothListsScoresHigherThanChunkInOneList() {
        UUID sharedId = UUID.randomUUID();
        UUID semanticOnlyId = UUID.randomUUID();
        UUID lexicalOnlyId = UUID.randomUUID();

        RetrievedChunk shared = chunk(sharedId, 0.9, 0.0);
        RetrievedChunk semanticOnly = chunk(semanticOnlyId, 0.7, 0.0);
        RetrievedChunk lexical = chunk(lexicalOnlyId, 0.0, 0.8);
        RetrievedChunk sharedLexical = chunk(sharedId, 0.0, 0.9);

        List<RetrievedChunk> result = fuser.fuse(
            List.of(shared, semanticOnly),
            List.of(sharedLexical, lexical));

        RetrievedChunk top = result.getFirst();
        assertThat(top.chunk().id()).isEqualTo(sharedId);
        assertThat(top.finalScore()).isGreaterThan(result.get(1).finalScore());
    }

    @Test
    void emptyListsReturnEmptyResult() {
        assertThat(fuser.fuse(List.of(), List.of())).isEmpty();
    }

    @Test
    void resultIsOrderedByRrfScoreDescending() {
        List<RetrievedChunk> semantic = List.of(
            chunk(UUID.randomUUID(), 0.9, 0.0),
            chunk(UUID.randomUUID(), 0.5, 0.0));
        List<RetrievedChunk> result = fuser.fuse(semantic, List.of());
        for (int i = 0; i < result.size() - 1; i++) {
            assertThat(result.get(i).finalScore()).isGreaterThanOrEqualTo(result.get(i + 1).finalScore());
        }
    }

    private RetrievedChunk chunk(UUID id, double semantic, double lexical) {
        KnowledgeChunk kc = new KnowledgeChunk(id, "tenant", UUID.randomUUID(), 0, "content", "source.txt");
        return new RetrievedChunk(kc, semantic, lexical, 0.0);
    }
}
