-- Make the application user non-superuser so FORCE ROW LEVEL SECURITY is fully enforced.
-- PostgreSQL bypasses RLS for superusers even with FORCE; this closes that loophole.
-- Exception is swallowed when the current user is the bootstrap superuser (Testcontainers).
DO $$
BEGIN
  IF EXISTS (SELECT 1 FROM pg_roles WHERE rolname = current_user AND rolsuper) THEN
    BEGIN
      EXECUTE 'ALTER ROLE ' || current_user || ' NOSUPERUSER';
    EXCEPTION
      WHEN insufficient_privilege THEN NULL;
      WHEN feature_not_supported THEN NULL;
    END;
  END IF;
END;
$$;

GRANT SELECT, INSERT, UPDATE, DELETE ON knowledge_chunk TO rag;
