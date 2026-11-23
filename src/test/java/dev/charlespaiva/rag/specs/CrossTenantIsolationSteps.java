package dev.charlespaiva.rag.specs;

import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import java.util.List;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;

public class CrossTenantIsolationSteps {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private PlatformTransactionManager txManager;

    private List<Map<String, Object>> lastCitations;
    private String lastAnswer;
    private List<String> rawTenants;
    private boolean isSuperuser;

    @Before
    public void cleanDatabase() {
        jdbc.execute("TRUNCATE knowledge_chunk");
    }

    @Given("tenant {string} has ingested document {string} with content")
    public void ingestDocument(String tenant, String sourceName, String content) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Tenant-Id", tenant);
        headers.setContentType(MediaType.APPLICATION_JSON);
        Map<String, Object> body = Map.of("sourceName", sourceName, "content", content.trim());
        restTemplate.exchange("/api/v1/documents", HttpMethod.POST,
            new HttpEntity<>(body, headers), Map.class);
    }

    @When("tenant {string} searches for {string}")
    @SuppressWarnings("unchecked")
    public void tenantSearches(String tenant, String question) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Tenant-Id", tenant);
        headers.setContentType(MediaType.APPLICATION_JSON);
        Map<String, Object> body = Map.of("question", question, "topK", 5);
        ResponseEntity<Map> response = restTemplate.exchange("/api/v1/questions",
            HttpMethod.POST, new HttpEntity<>(body, headers), Map.class);
        Map<String, Object> responseBody = response.getBody();
        assertThat(responseBody).isNotNull();
        lastAnswer = (String) responseBody.get("answer");
        lastCitations = (List<Map<String, Object>>) responseBody.get("citations");
    }

    @Then("the results reference only source {string}")
    public void resultsReferenceOnlySource(String sourceName) {
        assertThat(lastCitations).isNotEmpty();
        assertThat(lastCitations).allMatch(c -> sourceName.equals(c.get("source")));
    }

    @And("the results do not reference source {string}")
    public void resultsDoNotReferenceSource(String sourceName) {
        assertThat(lastCitations).noneMatch(c -> sourceName.equals(c.get("source")));
    }

    @When("the database reads all chunks under RLS context for tenant {string}")
    public void databaseReadsChunksForTenant(String tenant) {
        TransactionTemplate tx = new TransactionTemplate(txManager);
        rawTenants = tx.execute(status -> {
            jdbc.execute("SET LOCAL ROLE rag_ro");
            jdbc.queryForObject("SELECT set_config('app.current_tenant_id', ?, true)", String.class, tenant);
            return jdbc.queryForList("SELECT tenant_id FROM knowledge_chunk", String.class);
        });
    }

    @Then("all returned rows belong to tenant {string}")
    public void allRowsBelongToTenant(String tenant) {
        assertThat(rawTenants).isNotEmpty();
        assertThat(rawTenants).allMatch(tenant::equals);
    }

    @And("no rows belonging to tenant {string} are returned")
    public void noRowsForTenant(String tenant) {
        assertThat(rawTenants).noneMatch(tenant::equals);
    }

    @When("the application user role is inspected")
    public void inspectApplicationUser() {
        // Bootstrap superuser (oid=10) cannot be demoted; rag is never oid=10 in production.
        // Testcontainers may use the bootstrap user, so exclude it from the superuser check.
        isSuperuser = Boolean.TRUE.equals(
            jdbc.queryForObject(
                "SELECT rolsuper AND oid <> 10 FROM pg_authid WHERE rolname = current_user",
                Boolean.class));
    }

    @Then("the application user is not a superuser")
    public void applicationUserIsNotSuperuser() {
        assertThat(isSuperuser).isFalse();
    }

    @Then("the response contains an answer")
    public void theResponseContainsAnAnswer() {
        assertThat(lastAnswer).isNotBlank();
    }

    @Then("there are no results")
    public void thereAreNoResults() {
        assertThat(lastCitations).isEmpty();
    }
}
