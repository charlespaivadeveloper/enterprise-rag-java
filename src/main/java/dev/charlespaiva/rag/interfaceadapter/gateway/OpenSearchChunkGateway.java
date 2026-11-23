package dev.charlespaiva.rag.interfaceadapter.gateway;

import dev.charlespaiva.rag.entity.KnowledgeChunk;
import dev.charlespaiva.rag.usecase.common.RetrievedChunk;
import dev.charlespaiva.rag.usecase.dataaccess.ChunkDataAccessInterface;
import dev.charlespaiva.rag.usecase.dataaccess.DocumentSummaryData;
import dev.charlespaiva.rag.usecase.dataaccess.PagedResult;
import jakarta.annotation.PostConstruct;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.FieldValue;
import org.opensearch.client.opensearch._types.mapping.Property;
import org.opensearch.client.opensearch._types.query_dsl.Query;
import org.opensearch.client.opensearch.core.BulkRequest;
import org.opensearch.client.opensearch.core.DeleteByQueryRequest;
import org.opensearch.client.opensearch.core.SearchRequest;
import org.opensearch.client.opensearch.core.bulk.BulkOperation;
import org.opensearch.client.opensearch.core.bulk.IndexOperation;
import org.opensearch.client.opensearch.core.search.Hit;
import org.opensearch.client.opensearch.indices.CreateIndexRequest;
import org.opensearch.client.opensearch.indices.ExistsRequest;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Repository
@ConditionalOnProperty(name = "rag.search.backend", havingValue = "opensearch")
public class OpenSearchChunkGateway implements ChunkDataAccessInterface {

    static final String INDEX = "knowledge_chunk";

    private final OpenSearchClient client;

    public OpenSearchChunkGateway(OpenSearchClient client) {
        this.client = client;
    }

    @PostConstruct
    public void ensureIndex() {
        try {
            boolean exists = client.indices().exists(ExistsRequest.of(e -> e.index(INDEX))).value();
            if (!exists) {
                client.indices().create(CreateIndexRequest.of(cr -> cr
                    .index(INDEX)
                    .settings(s -> s.knn(true))
                    .mappings(m -> m
                        .properties("tenant_id", Property.of(p -> p.keyword(k -> k)))
                        .properties("document_id", Property.of(p -> p.keyword(k -> k)))
                        .properties("ordinal", Property.of(p -> p.integer(i -> i)))
                        .properties("content", Property.of(p -> p.text(t -> t)))
                        .properties("source_name", Property.of(p -> p.keyword(k -> k)))
                        .properties("created_at", Property.of(p -> p.date(d -> d)))
                        .properties("embedding", Property.of(p -> p.knnVector(kv -> kv.dimension(1536))))
                    )
                ));
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to ensure OpenSearch index: " + INDEX, e);
        }
    }

    @Override
    public void saveAll(List<KnowledgeChunk> chunks, List<float[]> embeddings) {
        if (chunks.size() != embeddings.size()) {
            throw new IllegalArgumentException("chunk/vector cardinality mismatch");
        }
        List<BulkOperation> ops = new ArrayList<>();
        for (int i = 0; i < chunks.size(); i++) {
            KnowledgeChunk c = chunks.get(i);
            Map<String, Object> doc = buildDoc(c, embeddings.get(i));
            ops.add(BulkOperation.of(b -> b.index(IndexOperation.of(io -> io
                .index(INDEX).id(c.id().toString()).document(doc)))));
        }
        try {
            client.bulk(BulkRequest.of(br -> br.operations(ops)));
        } catch (IOException e) {
            throw new RuntimeException("OpenSearch bulk index failed", e);
        }
    }

    @Override
    public List<RetrievedChunk> semanticSearch(String tenantId, float[] embedding, int limit) {
        Query tenantFilter = Query.of(qf -> qf.term(t -> t.field("tenant_id").value(FieldValue.of(tenantId))));
        SearchRequest request = SearchRequest.of(r -> r
            .index(INDEX)
            .query(q -> q.knn(k -> k
                .field("embedding")
                .vector(embedding)
                .k(limit)
                .filter(tenantFilter)))
            .size(limit));
        return executeSearch(request, 0.0, 0.0);
    }

    @Override
    public List<RetrievedChunk> lexicalSearch(String tenantId, String query, int limit) {
        SearchRequest request = SearchRequest.of(r -> r
            .index(INDEX)
            .query(q -> q.bool(b -> b
                .must(Query.of(m -> m.match(ma -> ma.field("content").query(FieldValue.of(query)))))
                .filter(Query.of(f -> f.term(t -> t.field("tenant_id").value(FieldValue.of(tenantId)))))))
            .size(limit));
        return executeSearch(request, 0.0, 0.0);
    }

    @Override
    public List<RetrievedChunk> hybridSearch(String tenantId, String query, float[] embedding, int limit) {
        List<RetrievedChunk> semantic = semanticSearch(tenantId, embedding, limit);
        List<RetrievedChunk> lexical = lexicalSearch(tenantId, query, limit);
        Map<UUID, RetrievedChunk> merged = new HashMap<>();
        semantic.forEach(r -> merged.put(r.chunk().id(), r));
        lexical.forEach(r -> merged.putIfAbsent(r.chunk().id(), r));
        return new ArrayList<>(merged.values());
    }

    @Override
    public PagedResult<DocumentSummaryData> listDocuments(String tenantId, int page, int size) {
        SearchRequest request = SearchRequest.of(r -> r
            .index(INDEX)
            .query(q -> q.term(t -> t.field("tenant_id").value(FieldValue.of(tenantId))))
            .size(size).from(page * size));
        try {
            var response = client.search(request, Map.class);
            List<DocumentSummaryData> items = new ArrayList<>();
            for (Hit<Map> hit : response.hits().hits()) {
                Map<?, ?> src = hit.source();
                if (src != null) {
                    items.add(new DocumentSummaryData(
                        UUID.fromString((String) src.get("document_id")),
                        (String) src.get("source_name"),
                        1,
                        Instant.now()));
                }
            }
            long total = response.hits().total() != null ? response.hits().total().value() : 0L;
            return new PagedResult<>(items, total);
        } catch (IOException e) {
            throw new RuntimeException("OpenSearch listDocuments failed", e);
        }
    }

    @Override
    public int deleteByDocument(String tenantId, UUID documentId) {
        try {
            var response = client.deleteByQuery(DeleteByQueryRequest.of(d -> d
                .index(INDEX)
                .query(q -> q.bool(b -> b
                    .must(Query.of(m -> m.term(t -> t.field("document_id").value(FieldValue.of(documentId.toString())))))
                    .filter(Query.of(f -> f.term(t -> t.field("tenant_id").value(FieldValue.of(tenantId)))))))));
            Long deleted = response.deleted();
            return deleted != null ? deleted.intValue() : 0;
        } catch (IOException e) {
            throw new RuntimeException("OpenSearch deleteByDocument failed", e);
        }
    }

    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    private List<RetrievedChunk> executeSearch(SearchRequest request, double defaultSemantic,
                                                double defaultLexical) {
        try {
            var response = client.search(request, Map.class);
            List<RetrievedChunk> results = new ArrayList<>();
            for (Hit<Map> hit : response.hits().hits()) {
                Map<?, ?> src = hit.source();
                if (src != null) {
                    KnowledgeChunk chunk = new KnowledgeChunk(
                        UUID.fromString((String) src.get("id")),
                        (String) src.get("tenant_id"),
                        UUID.fromString((String) src.get("document_id")),
                        ((Number) src.get("ordinal")).intValue(),
                        (String) src.get("content"),
                        (String) src.get("source_name"));
                    double score = hit.score() != null ? hit.score() : 0.0;
                    results.add(new RetrievedChunk(chunk, defaultSemantic > 0 ? score : defaultSemantic,
                        defaultLexical > 0 ? score : defaultLexical, score));
                }
            }
            return results;
        } catch (IOException e) {
            throw new RuntimeException("OpenSearch search failed", e);
        }
    }

    private Map<String, Object> buildDoc(KnowledgeChunk c, float... embedding) {
        Map<String, Object> doc = new HashMap<>();
        doc.put("id", c.id().toString());
        doc.put("tenant_id", c.tenantId());
        doc.put("document_id", c.documentId().toString());
        doc.put("ordinal", c.ordinal());
        doc.put("content", c.content());
        doc.put("source_name", c.sourceName());
        doc.put("created_at", Instant.now().toString());
        doc.put("embedding", toFloatList(embedding));
        return doc;
    }

    private List<Float> toFloatList(float... arr) {
        List<Float> list = new ArrayList<>(arr.length);
        for (float v : arr) {
            list.add(v);
        }
        return list;
    }
}
