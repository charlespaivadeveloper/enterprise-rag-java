package dev.charlespaiva.rag.interfaceadapter.gateway;

import dev.charlespaiva.rag.usecase.common.RetrievedChunk;
import dev.charlespaiva.rag.usecase.dataaccess.RerankingServiceInterface;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
@ConditionalOnProperty(name = "rag.reranking.enabled", havingValue = "false", matchIfMissing = true)
public class NoOpRerankingAdapter implements RerankingServiceInterface {

    @Override
    public List<RetrievedChunk> rerank(String query, List<RetrievedChunk> candidates) {
        return candidates;
    }
}
