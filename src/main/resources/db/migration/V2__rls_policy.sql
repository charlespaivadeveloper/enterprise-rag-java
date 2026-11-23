-- Enforce RLS on the table owner (the application user) so it cannot bypass isolation.
-- The application must call: SELECT set_config('app.current_tenant_id', tenantId, true)
-- at the start of every transaction before accessing this table.
alter table knowledge_chunk force row level security;

create policy tenant_isolation
    on knowledge_chunk
    using (tenant_id = current_setting('app.current_tenant_id', true));

-- Non-superuser read role used in RLS integration tests to verify policy enforcement.
-- Superusers bypass RLS even with FORCE ROW LEVEL SECURITY, so tests must
-- SET LOCAL ROLE rag_ro to become a non-superuser subject to the policy.
create role rag_ro nologin;
grant select on knowledge_chunk to rag_ro;
