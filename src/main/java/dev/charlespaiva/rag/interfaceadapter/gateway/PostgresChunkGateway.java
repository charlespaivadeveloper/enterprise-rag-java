package dev.charlespaiva.rag.interfaceadapter.gateway;

import dev.charlespaiva.rag.entity.KnowledgeChunk;
import dev.charlespaiva.rag.usecase.common.RetrievedChunk;
import dev.charlespaiva.rag.usecase.dataaccess.ChunkDataAccessInterface;
import dev.charlespaiva.rag.usecase.dataaccess.DocumentSummaryData;
import dev.charlespaiva.rag.usecase.dataaccess.PagedResult;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.UUID;

@Repository
@ConditionalOnProperty(name = "rag.search.backend", havingValue = "pgvector", matchIfMissing = true)
public class PostgresChunkGateway implements ChunkDataAccessInterface {

    private final JdbcTemplate jdbc;

    public PostgresChunkGateway(JdbcTemplate j) {
        jdbc = j;
    }

    @Override
    @Transactional
    public void saveAll(List<KnowledgeChunk> chunks, List<float[]> vectors) {
        if (chunks.size() != vectors.size()) {
            throw new IllegalArgumentException("chunk/vector cardinality mismatch");
        }
        TenantContextSetter.set(jdbc, chunks.get(0).tenantId());
        for (int i = 0; i < chunks.size(); i++) {
            KnowledgeChunk c = chunks.get(i);
            jdbc.update("insert into knowledge_chunk(id,tenant_id,document_id,ordinal,content,source_name,embedding) values (?,?,?,?,?,?,?::vector)",
                c.id(), c.tenantId(), c.documentId(), c.ordinal(), c.content(), c.sourceName(), toVector(vectors.get(i)));
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<RetrievedChunk> hybridSearch(String tenantId, String query, float[] embedding, int limit) {
        TenantContextSetter.set(jdbc, tenantId);
        String sql = """
            with ranked as (select *, 1-(embedding <=> ?::vector) semantic_score,
              ts_rank_cd(search_vector, websearch_to_tsquery('simple', ?)) lexical_score
              from knowledge_chunk where tenant_id=?), scored as
            (select *, (semantic_score*0.70 + least(lexical_score,1)*0.30) final_score from ranked)
            select * from scored where semantic_score > 0.15 or lexical_score > 0 order by final_score desc limit ?
            """;
        return jdbc.query(sql, (rs, n) -> {
            KnowledgeChunk c = mapChunk(rs);
            return new RetrievedChunk(c, rs.getDouble("semantic_score"), rs.getDouble("lexical_score"), rs.getDouble("final_score"));
        }, toVector(embedding), query, tenantId, limit);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RetrievedChunk> semanticSearch(String tenantId, float[] embedding, int limit) {
        TenantContextSetter.set(jdbc, tenantId);
        String sql = """
            select *, 1-(embedding <=> ?::vector) semantic_score, 0.0 lexical_score
            from knowledge_chunk where tenant_id = ?
            order by embedding <=> ?::vector limit ?
            """;
        String vec = toVector(embedding);
        return jdbc.query(sql, (rs, n) -> {
            KnowledgeChunk c = mapChunk(rs);
            return new RetrievedChunk(c, rs.getDouble("semantic_score"), 0.0, 0.0);
        }, vec, tenantId, vec, limit);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RetrievedChunk> lexicalSearch(String tenantId, String query, int limit) {
        TenantContextSetter.set(jdbc, tenantId);
        String sql = """
            select *, 0.0 semantic_score, ts_rank_cd(search_vector, websearch_to_tsquery('simple', ?)) lexical_score
            from knowledge_chunk where tenant_id = ? and search_vector @@ websearch_to_tsquery('simple', ?)
            order by lexical_score desc limit ?
            """;
        return jdbc.query(sql, (rs, n) -> {
            KnowledgeChunk c = mapChunk(rs);
            return new RetrievedChunk(c, 0.0, rs.getDouble("lexical_score"), 0.0);
        }, query, tenantId, query, limit);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResult<DocumentSummaryData> listDocuments(String tenantId, int page, int size) {
        TenantContextSetter.set(jdbc, tenantId);
        long total = Objects.requireNonNullElse(
            jdbc.queryForObject(
                "select count(distinct document_id) from knowledge_chunk where tenant_id = ?",
                Long.class, tenantId),
            0L);
        String sql = """
            select document_id, source_name, count(*) chunk_count, min(created_at) created_at
            from knowledge_chunk where tenant_id = ?
            group by document_id, source_name
            order by min(created_at) desc
            limit ? offset ?
            """;
        List<DocumentSummaryData> items = jdbc.query(sql, (rs, n) -> new DocumentSummaryData(
            rs.getObject("document_id", UUID.class),
            rs.getString("source_name"),
            rs.getInt("chunk_count"),
            rs.getTimestamp("created_at").toInstant()),
            tenantId, size, (long) page * size);
        return new PagedResult<>(items, total);
    }

    @Override
    @Transactional
    public int deleteByDocument(String tenantId, UUID documentId) {
        TenantContextSetter.set(jdbc, tenantId);
        return jdbc.update(
            "delete from knowledge_chunk where tenant_id = ? and document_id = ?",
            tenantId, documentId);
    }

    private KnowledgeChunk mapChunk(ResultSet rs) throws SQLException {
        return new KnowledgeChunk(rs.getObject("id", UUID.class), rs.getString("tenant_id"),
            rs.getObject("document_id", UUID.class), rs.getInt("ordinal"),
            rs.getString("content"), rs.getString("source_name"));
    }

    private String toVector(float... v) {
        StringJoiner j = new StringJoiner(",", "[", "]");
        for (float x : v) {
            j.add(Float.toString(x));
        }
        return j.toString();
    }
}
