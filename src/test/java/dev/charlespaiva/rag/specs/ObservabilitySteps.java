package dev.charlespaiva.rag.specs;

import io.cucumber.java.Before;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class ObservabilitySteps {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private JdbcTemplate jdbc;

    private ResponseEntity<Map> lastResponse;

    @Before
    public void cleanDatabase() {
        jdbc.execute("TRUNCATE knowledge_chunk");
    }

    @When("tenant {string} sends an ingest request for {string} with content {string}")
    public void sendIngestRequest(String tenant, String sourceName, String content) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Tenant-Id", tenant);
        headers.set("Content-Type", "application/json");
        String body = "{\"sourceName\":\"" + sourceName + "\",\"content\":\"" + content + "\"}";
        lastResponse = restTemplate.exchange("/api/v1/documents", HttpMethod.POST,
            new HttpEntity<>(body, headers), Map.class);
    }

    @When("tenant {string} sends an ask request with question {string}")
    public void sendAskRequest(String tenant, String question) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Tenant-Id", tenant);
        headers.set("Content-Type", "application/json");
        String body = "{\"question\":\"" + question + "\",\"topK\":1}";
        lastResponse = restTemplate.exchange("/api/v1/questions", HttpMethod.POST,
            new HttpEntity<>(body, headers), Map.class);
    }

    @Then("the response includes an {string} header")
    public void responseIncludesHeader(String headerName) {
        assertThat(lastResponse.getHeaders().containsKey(headerName))
            .as("Expected response to contain header '%s'", headerName)
            .isTrue();
        assertThat(lastResponse.getHeaders().getFirst(headerName)).isNotBlank();
    }
}
