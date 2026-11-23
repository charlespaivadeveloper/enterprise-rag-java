create table ingest_job (
    id uuid primary key,
    tenant_id varchar(80) not null,
    source_name varchar(255) not null,
    status varchar(20) not null default 'PENDING',
    failure_reason text,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

create index idx_ingest_job_tenant on ingest_job(tenant_id);

alter table ingest_job enable row level security;
alter table ingest_job force row level security;

create policy ingest_job_isolation
    on ingest_job
    using (tenant_id = current_setting('app.current_tenant_id', true));

grant select, insert, update on ingest_job to rag;
