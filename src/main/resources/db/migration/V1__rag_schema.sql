create extension if not exists vector;
create table knowledge_chunk (
  id uuid primary key,
  tenant_id varchar(80) not null,
  document_id uuid not null,
  ordinal integer not null,
  content text not null,
  source_name varchar(255) not null,
  embedding vector(1536) not null,
  search_vector tsvector generated always as (to_tsvector('simple', content)) stored,
  created_at timestamptz not null default now(),
  unique(tenant_id, document_id, ordinal)
);
create index idx_chunk_tenant on knowledge_chunk(tenant_id);
create index idx_chunk_fts on knowledge_chunk using gin(search_vector);
create index idx_chunk_embedding_hnsw on knowledge_chunk using hnsw (embedding vector_cosine_ops);
alter table knowledge_chunk enable row level security;
