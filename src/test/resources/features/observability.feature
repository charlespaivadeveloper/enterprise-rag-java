Feature: Observability — trace ID propagation
  As an SRE
  I want every HTTP response to include a trace ID header
  So that I can correlate requests across services in distributed traces

  Scenario: Ingest request returns X-Trace-Id header
    When tenant "acme" sends an ingest request for "obs-doc.txt" with content "observability test content"
    Then the response includes an "X-Trace-Id" header

  Scenario: Ask request returns X-Trace-Id header
    When tenant "acme" sends an ask request with question "what is observability?"
    Then the response includes an "X-Trace-Id" header
