Feature: Cross-tenant data isolation
  As a platform operator
  I want each tenant's knowledge base to be strictly isolated
  So that one tenant cannot access or contaminate another's data

  Background:
    Given tenant "acme" has ingested document "acme-policy.txt" with content
      """
      Acme vacation policy: 20 days per year for all full-time employees.
      """
    And tenant "globex" has ingested document "globex-policy.txt" with content
      """
      Globex annual leave policy: 30 days for all permanent staff.
      """

  Scenario: A tenant retrieves only their own documents
    When tenant "acme" searches for "vacation"
    Then the results reference only source "acme-policy.txt"
    And the results do not reference source "globex-policy.txt"

  Scenario: A tenant cannot see another tenant's documents
    When tenant "globex" searches for "annual leave"
    Then the results reference only source "globex-policy.txt"
    And the results do not reference source "acme-policy.txt"

  Scenario: RLS enforces isolation at the database layer
    When the database reads all chunks under RLS context for tenant "acme"
    Then all returned rows belong to tenant "acme"
    And no rows belonging to tenant "globex" are returned

  Scenario: Application user is not a superuser (durable RLS)
    When the application user role is inspected
    Then the application user is not a superuser
