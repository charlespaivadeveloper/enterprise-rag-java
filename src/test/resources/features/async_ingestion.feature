Feature: Asynchronous document ingestion
  As a tenant
  I want to submit documents for background ingestion
  So that the API returns immediately without blocking

  Scenario: Submitting a document returns a job ID immediately
    When tenant "acme" submits document "policy.pdf" for async ingestion with content "Vacation policy: 20 days"
    Then the async response status is 202
    And the async response contains a jobId

  Scenario: Job status transitions to COMPLETED after processing
    Given tenant "acme" has submitted document "quick.pdf" for async ingestion with content "Quick document"
    When the ingestion job completes
    Then the job status is "COMPLETED"
    And tenant "acme" can find document "quick.pdf" in their document list
