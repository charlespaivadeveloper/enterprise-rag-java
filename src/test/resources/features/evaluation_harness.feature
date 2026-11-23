Feature: RAG evaluation harness
  As a platform engineer
  I want to measure retrieval quality against a golden dataset
  So that regressions are caught automatically in CI

  Scenario: Evaluation report meets quality thresholds on the golden dataset
    Given tenant "eval-tenant" has ingested document "hr-policy.txt" with content
      """
      Acme vacation policy: employees receive 20 days of paid annual leave per year.
      Full-time employees accrue leave at 1.67 days per month.
      """
    When the evaluation is run with dataset "golden-v1" and topK 5
    Then the mean reciprocal rank is at least 0.50
    And the mean NDCG at 5 is at least 0.50

  Scenario: Injection samples are marked as refused in the report
    Given tenant "eval-tenant" has ingested document "hr-policy.txt" with content
      """
      Acme vacation policy: 20 days per year.
      """
    When the evaluation is run with dataset "golden-v1" and topK 5
    Then all injection samples are marked as refused
