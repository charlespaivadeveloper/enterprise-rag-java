package dev.charlespaiva.rag.specs;

import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.jdbc.core.JdbcTemplate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class PiiRedactionSteps {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private JdbcTemplate jdbc;

    @Before
    public void cleanDatabase() {
        jdbc.execute("TRUNCATE knowledge_chunk");
    }

    @Given("tenant {string} ingests a document {string} with content {string}")
    public void ingestDocument(String tenant, String sourceName, String content) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Tenant-Id", tenant);
        headers.set("Content-Type", "application/json");
        String body = "{\"sourceName\":\"" + sourceName + "\",\"content\":\"" + content + "\"}";
        restTemplate.exchange("/api/v1/documents", HttpMethod.POST,
            new HttpEntity<>(body, headers), Map.class);
    }

    @Then("the stored chunk for tenant {string} does not contain {string}")
    public void chunkDoesNotContain(String tenant, String text) {
        String stored = jdbc.queryForObject(
            "SELECT content FROM knowledge_chunk WHERE tenant_id = ? LIMIT 1",
            String.class, tenant);
        assertThat(stored).doesNotContain(text);
    }

    @Then("the stored chunk for tenant {string} contains {string}")
    public void chunkContains(String tenant, String text) {
        String stored = jdbc.queryForObject(
            "SELECT content FROM knowledge_chunk WHERE tenant_id = ? LIMIT 1",
            String.class, tenant);
        assertThat(stored).contains(text);
    }
}
