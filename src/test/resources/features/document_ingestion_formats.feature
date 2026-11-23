Feature: Multi-format document ingestion
  As a tenant
  I want to ingest PDF and DOCX files
  So that I can query my document library regardless of format

  Scenario: PDF document is ingested and retrievable
    Given tenant "acme" uploads a PDF file "policy.pdf" with text "Vacation: 20 days per year for all full-time employees"
    When tenant "acme" searches for "vacation days"
    Then the results reference only source "policy.pdf"

  Scenario: DOCX document is ingested and retrievable
    Given tenant "acme" uploads a DOCX file "handbook.docx" with text "Annual leave: employees accrue 25 days per year"
    When tenant "acme" searches for "annual leave"
    Then the results reference only source "handbook.docx"

  Scenario: Unsupported media type is rejected with 400
    Given tenant "acme" uploads a file "data.csv" with content type "text/csv" and bytes "col1,col2"
    Then the ingestion response status is 400
