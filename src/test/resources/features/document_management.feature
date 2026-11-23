Feature: Document management
  As a tenant
  I want to list and delete my ingested documents
  So that I can manage my knowledge base

  Scenario: Listing documents shows ingested documents for the tenant
    Given tenant "acme" has ingested document "policy.pdf" with content
      """
      Acme vacation policy: 20 days per year for all full-time employees.
      """
    When tenant "acme" lists their documents
    Then the document list contains source "policy.pdf"

  Scenario: Cross-tenant isolation — tenant sees only its own documents
    Given tenant "alpha" has ingested document "alpha.pdf" with content
      """
      Alpha company confidential data.
      """
    When tenant "beta" lists their documents
    Then the document list is empty

  Scenario: Deleting a document removes it from the list
    Given tenant "acme" has ingested document "temp.pdf" with content
      """
      Temporary document that will be deleted.
      """
    When tenant "acme" deletes source "temp.pdf"
    Then the document list does not contain source "temp.pdf"

  Scenario: Page size limits the number of results returned
    Given tenant "acme" has ingested document "doc1.pdf" with content
      """
      First document content for pagination test.
      """
    And tenant "acme" has ingested document "doc2.pdf" with content
      """
      Second document content for pagination test.
      """
    When tenant "acme" lists their documents with page 0 and size 1
    Then the current page has 1 document
    And the total elements is 2
