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
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class EvaluationHarnessSteps {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private JdbcTemplate jdbc;

    private Map<String, Object> lastReport;

    @Before
    public void cleanDatabase() {
        jdbc.execute("TRUNCATE knowledge_chunk");
    }

    @When("the evaluation is run with dataset {string} and topK {int}")
    @SuppressWarnings("unchecked")
    public void runEvaluation(String dataset, int topK) {
        HttpHeaders headers = new HttpHeaders();
        ResponseEntity<Map> response = restTemplate.exchange(
            "/api/v1/eval/run?dataset=" + dataset + "&topK=" + topK,
            HttpMethod.POST, new HttpEntity<>(headers), Map.class);
        assertThat(response.getBody()).isNotNull();
        lastReport = response.getBody();
    }

    @Then("the mean reciprocal rank is at least {double}")
    public void meanReciprocalRankAtLeast(double threshold) {
        double mrr = ((Number) lastReport.get("meanReciprocalRank")).doubleValue();
        assertThat(mrr).isGreaterThanOrEqualTo(threshold);
    }

    @And("the mean NDCG at {int} is at least {double}")
    public void meanNdcgAtLeast(int ignored, double threshold) {
        double ndcg = ((Number) lastReport.get("meanNdcg")).doubleValue();
        assertThat(ndcg).isGreaterThanOrEqualTo(threshold);
    }

    @And("all injection samples are marked as refused")
    @SuppressWarnings("unchecked")
    public void allInjectionSamplesRefused() {
        List<Map<String, Object>> results = (List<Map<String, Object>>) lastReport.get("results");
        assertThat(results).isNotNull();
        List<Map<String, Object>> injections = results.stream()
            .filter(r -> "refused".equals(r.get("actualAnswer")))
            .toList();
        assertThat(injections).isNotEmpty();
        injections.forEach(r -> assertThat((Boolean) r.get("groundednessPass")).isTrue());
    }
}
