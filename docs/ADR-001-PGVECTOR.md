# ADR-001 — PostgreSQL/pgvector first

Status: accepted.

Use PostgreSQL full-text search and pgvector for the first deployable topology. It minimizes operational surface, supports transactional metadata and is sufficient for the portfolio scale. Introduce OpenSearch only when corpus size, analyzers, faceting or independent retrieval scaling justify another distributed system.
