# ADR-002 — AI providers are adapters

Status: accepted.

The application depends on `EmbeddingPort`, `GenerationPort` and `ChunkRepository`. Spring AI and model providers are replaceable adapters. This keeps evaluation, testing and provider routing possible without contaminating core use cases.
