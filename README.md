# Enterprise RAG Platform — v0.1.1

Production-minded Java reference for multi-tenant retrieval-augmented generation. The first vertical slice ingests text, chunks and embeds it, performs PostgreSQL hybrid retrieval, and returns a grounded answer with citations.

## What works in this iteration

- Text ingestion and deterministic overlapping chunking.
- OpenAI embeddings/chat through Spring AI; offline `local-stub` profile.
- PostgreSQL full-text + pgvector cosine search with weighted fusion.
- Tenant filter applied in every persistence operation.
- Source, chunk ordinal, excerpt and score returned as citations.
- Query injection detection and explicit untrusted-evidence prompt boundary.
- Actuator, Prometheus, graceful shutdown, CI and architecture tests.
- IntelliJ shared code style, EditorConfig, Checkstyle, PMD and SpotBugs quality gates.

## Run locally

Requirements: Java 21 and Docker.

```bash
docker compose up -d postgres
SPRING_PROFILES_ACTIVE=local-stub mvn spring-boot:run
```

Ingest:

```bash
curl -X POST localhost:8080/api/v1/documents -H 'Content-Type: application/json' -H 'X-Tenant-Id: acme' -d '{"sourceName":"policy.txt","content":"Employees receive thirty days of annual leave after twelve months."}'
```

Ask:

```bash
curl -X POST localhost:8080/api/v1/questions -H 'Content-Type: application/json' -H 'X-Tenant-Id: acme' -d '{"question":"How much annual leave is provided?","topK":5}'
```

## Important security statement

`X-Tenant-Id` is a demo transport mechanism, not authentication. Production must derive tenant identity from a verified JWT/mTLS principal and set a PostgreSQL transaction context for RLS. Regexes are defense-in-depth, not a complete prompt-injection solution.

## Next iteration

PDF/DOCX readers, asynchronous ingestion, durable document status, real RLS policies, reciprocal-rank fusion, pluggable cross-encoder reranker, RAGAS-style evaluation dataset and Testcontainers integration.

See [`docs/`](docs/) for architecture, decisions, threat model and roadmap.

## Code style and static analysis

IntelliJ automatically reads `.editorconfig` and the committed project code style. Use **Code > Reformat Code** (`Ctrl+Alt+L` on Windows/Linux). The profile favors compact formatting with a 180-column right margin and keeps simple statements on one line when IntelliJ can do so safely.

```bash
mvn checkstyle:check
mvn pmd:check pmd:cpd-check
mvn compile spotbugs:check
mvn verify
```

For complete IDE integration, follow [`docs/INTELLIJ-CODE-QUALITY.md`](docs/INTELLIJ-CODE-QUALITY.md). SpotBugs is the maintained successor to FindBugs.
