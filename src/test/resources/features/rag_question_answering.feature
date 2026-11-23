Feature: RAG question answering
  As a tenant
  I want to ask questions about my ingested documents
  So that I get grounded answers with source citations

  Scenario: Answer returned with citations for matching content
    Given tenant "acme" has ingested document "hr-policy.pdf" with content
      """
      Acme vacation policy: 20 days per year for all full-time employees.
      """
    When tenant "acme" searches for "vacation"
    Then the results reference only source "hr-policy.pdf"
    And the response contains an answer

  Scenario: No citations returned when tenant has no ingested documents
    When tenant "ghost-corp" searches for "vacation policy"
    Then there are no results
