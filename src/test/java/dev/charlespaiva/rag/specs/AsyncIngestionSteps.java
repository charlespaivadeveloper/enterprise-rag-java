package dev.charlespaiva.rag.specs;

import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.awaitility.Awaitility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class AsyncIngestionSteps {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private JdbcTemplate jdbc;

    private int lastAsyncStatus;
    private UUID lastJobId;
    private String lastTenant;

    @Before
    public void cleanDatabase() {
        jdbc.execute("TRUNCATE knowledge_chunk");
        jdbc.execute("TRUNCATE ingest_job");
    }

    @When("tenant {string} submits document {string} for async ingestion with content {string}")
    @SuppressWarnings("unchecked")
    public void submitAsync(String tenant, String sourceName, String content) {
        lastTenant = tenant;
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Tenant-Id", tenant);
        headers.setContentType(MediaType.APPLICATION_JSON);
        Map<String, Object> body = Map.of("sourceName", sourceName, "content", content);
        ResponseEntity<Map> response = restTemplate.exchange("/api/v1/documents/async", HttpMethod.POST,
            new HttpEntity<>(body, headers), Map.class);
        lastAsyncStatus = response.getStatusCode().value();
        if (response.getBody() != null) {
            lastJobId = UUID.fromString((String) response.getBody().get("jobId"));
        }
    }

    @Given("tenant {string} has submitted document {string} for async ingestion with content {string}")
    public void hasSubmittedAsync(String tenant, String sourceName, String content) {
        submitAsync(tenant, sourceName, content);
    }

    @Then("the async response status is {int}")
    public void asyncResponseStatus(int expected) {
        assertThat(lastAsyncStatus).isEqualTo(expected);
    }

    @And("the async response contains a jobId")
    public void asyncResponseContainsJobId() {
        assertThat(lastJobId).isNotNull();
    }

    @When("the ingestion job completes")
    public void waitForJobCompletion() {
        Awaitility.await()
            .atMost(Duration.ofSeconds(30))
            .pollInterval(Duration.ofMillis(500))
            .until(() -> "COMPLETED".equals(fetchJobStatus()));
    }

    @Then("the job status is {string}")
    public void jobStatusIs(String expectedStatus) {
        assertThat(fetchJobStatus()).isEqualTo(expectedStatus);
    }

    @And("tenant {string} can find document {string} in their document list")
    @SuppressWarnings("unchecked")
    public void tenantCanFindDocument(String tenant, String sourceName) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Tenant-Id", tenant);
        ResponseEntity<Map> response = restTemplate.exchange("/api/v1/documents?page=0&size=20",
            HttpMethod.GET, new HttpEntity<>(headers), Map.class);
        assertThat(response.getBody()).isNotNull();
        var content = (java.util.List<Map<String, Object>>) response.getBody().get("content");
        assertThat(content).anyMatch(d -> sourceName.equals(d.get("sourceName")));
    }

    @SuppressWarnings("unchecked")
    private String fetchJobStatus() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Tenant-Id", lastTenant);
        ResponseEntity<Map> response = restTemplate.exchange(
            "/api/v1/documents/jobs/" + lastJobId, HttpMethod.GET,
            new HttpEntity<>(headers), Map.class);
        if (response.getBody() == null) {
            return null;
        }
        return (String) response.getBody().get("status");
    }
}
