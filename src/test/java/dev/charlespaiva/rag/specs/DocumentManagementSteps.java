package dev.charlespaiva.rag.specs;

import io.cucumber.java.Before;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.jdbc.core.JdbcTemplate;
import java.util.List;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;

public class DocumentManagementSteps {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private JdbcTemplate jdbc;

    private List<Map<String, Object>> lastDocumentList;
    private long lastTotalElements;

    @Before
    public void cleanDatabase() {
        jdbc.execute("TRUNCATE knowledge_chunk");
    }

    @When("tenant {string} lists their documents")
    public void tenantListsDocuments(String tenant) {
        fetchPage(tenant, 0, 20);
    }

    @When("tenant {string} lists their documents with page {int} and size {int}")
    public void tenantListsDocumentsWithPageAndSize(String tenant, int page, int size) {
        fetchPage(tenant, page, size);
    }

    @When("tenant {string} deletes source {string}")
    @SuppressWarnings("unchecked")
    public void tenantDeletesSource(String tenant, String sourceName) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Tenant-Id", tenant);
        var listResponse = restTemplate.exchange(
            "/api/v1/documents?page=0&size=100", HttpMethod.GET,
            new HttpEntity<>(headers), Map.class);
        Map<String, Object> body = listResponse.getBody();
        assertThat(body).isNotNull();
        List<Map<String, Object>> docs = (List<Map<String, Object>>) body.get("content");
        String documentId = docs.stream()
            .filter(d -> sourceName.equals(d.get("sourceName")))
            .map(d -> (String) d.get("documentId"))
            .findFirst()
            .orElseThrow(() -> new AssertionError("document not found: " + sourceName));
        restTemplate.exchange(
            "/api/v1/documents/" + documentId, HttpMethod.DELETE,
            new HttpEntity<>(headers), Void.class);
        fetchPage(tenant, 0, 100);
    }

    @Then("the document list contains source {string}")
    public void documentListContainsSource(String sourceName) {
        assertThat(lastDocumentList).isNotNull();
        assertThat(lastDocumentList).anyMatch(d -> sourceName.equals(d.get("sourceName")));
    }

    @Then("the document list does not contain source {string}")
    public void documentListDoesNotContainSource(String sourceName) {
        assertThat(lastDocumentList).isNotNull();
        assertThat(lastDocumentList).noneMatch(d -> sourceName.equals(d.get("sourceName")));
    }

    @Then("the document list is empty")
    public void documentListIsEmpty() {
        assertThat(lastDocumentList).isNotNull().isEmpty();
    }

    @Then("the current page has {int} document(s)")
    public void currentPageHasDocuments(int count) {
        assertThat(lastDocumentList).hasSize(count);
    }

    @Then("the total elements is {int}")
    public void totalElementsIs(int expected) {
        assertThat(lastTotalElements).isEqualTo(expected);
    }

    @SuppressWarnings("unchecked")
    private void fetchPage(String tenant, int page, int size) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Tenant-Id", tenant);
        var response = restTemplate.exchange(
            "/api/v1/documents?page=" + page + "&size=" + size, HttpMethod.GET,
            new HttpEntity<>(headers), Map.class);
        Map<String, Object> body = response.getBody();
        assertThat(body).isNotNull();
        lastDocumentList = (List<Map<String, Object>>) body.get("content");
        lastTotalElements = ((Number) body.get("totalElements")).longValue();
    }
}
