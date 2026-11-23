package dev.charlespaiva.rag.interfaceadapter.gateway;

import org.springframework.jdbc.core.JdbcTemplate;

final class TenantContextSetter {

    private TenantContextSetter() {
    }

    static void set(JdbcTemplate jdbc, String tenantId) {
        jdbc.queryForObject("SELECT set_config('app.current_tenant_id', ?, true)", String.class, tenantId);
    }
}
