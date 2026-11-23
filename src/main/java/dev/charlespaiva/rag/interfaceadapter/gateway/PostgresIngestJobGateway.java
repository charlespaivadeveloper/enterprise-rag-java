package dev.charlespaiva.rag.interfaceadapter.gateway;

import dev.charlespaiva.rag.usecase.ingest.IngestJob;
import dev.charlespaiva.rag.usecase.ingest.IngestJobRepositoryPort;
import dev.charlespaiva.rag.usecase.ingest.IngestJobStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class PostgresIngestJobGateway implements IngestJobRepositoryPort {

    private final JdbcTemplate jdbc;

    public PostgresIngestJobGateway(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    @Transactional
    public void save(IngestJob job) {
        TenantContextSetter.set(jdbc, job.tenantId());
        jdbc.update(
            "INSERT INTO ingest_job(id, tenant_id, source_name, status) VALUES (?, ?, ?, ?)",
            job.jobId(), job.tenantId(), job.sourceName(), job.status().name());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<IngestJob> findById(UUID jobId, String tenantId) {
        TenantContextSetter.set(jdbc, tenantId);
        List<IngestJob> results = jdbc.query(
            "SELECT id, tenant_id, source_name, status, created_at, failure_reason FROM ingest_job WHERE id = ?",
            (rs, n) -> new IngestJob(
                rs.getObject("id", UUID.class),
                rs.getString("tenant_id"),
                rs.getString("source_name"),
                IngestJobStatus.valueOf(rs.getString("status")),
                rs.getTimestamp("created_at").toInstant(),
                rs.getString("failure_reason")),
            jobId);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.getFirst());
    }

    @Override
    @Transactional
    public void updateStatus(UUID jobId, String tenantId, IngestJobStatus status, String failureReason) {
        TenantContextSetter.set(jdbc, tenantId);
        jdbc.update(
            "UPDATE ingest_job SET status = ?, failure_reason = ?, updated_at = now() WHERE id = ?",
            status.name(), failureReason, jobId);
    }
}
