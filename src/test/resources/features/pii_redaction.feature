Feature: PII redaction on document ingestion
  As a compliance officer
  I want PII stripped from documents before they are stored
  So that sensitive data is never persisted in the vector store

  Scenario: Email address is redacted from ingested content
    Given tenant "acme" ingests a document "privacy-test.txt" with content "Contact: john.doe@example.com is the owner"
    Then the stored chunk for tenant "acme" does not contain "john.doe@example.com"
    And the stored chunk for tenant "acme" contains "[EMAIL]"

  Scenario: Social Security Number is redacted from ingested content
    Given tenant "acme" ingests a document "hr-record.txt" with content "Employee SSN is 123-45-6789 per HR"
    Then the stored chunk for tenant "acme" does not contain "123-45-6789"
    And the stored chunk for tenant "acme" contains "[SSN]"

  Scenario: Phone number is redacted from ingested content
    Given tenant "acme" ingests a document "contact-sheet.txt" with content "Reach us at 555-867-5309 anytime"
    Then the stored chunk for tenant "acme" does not contain "555-867-5309"
    And the stored chunk for tenant "acme" contains "[PHONE]"
