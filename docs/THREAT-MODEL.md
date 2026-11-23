# Threat model

| Threat | Initial control | Remaining work |
|---|---|---|
| Cross-tenant retrieval | mandatory tenant predicate | verified identity + RLS policy tests |
| Prompt injection in query | validation and detection | classifier, policy engine, red-team corpus |
| Instructions hidden in documents | untrusted-evidence delimiters and role neutralization | ingestion classifier/quarantine |
| Hallucinated sources | citations generated only from retrieved records | entailment and citation precision evaluation |
| Data exfiltration to model provider | adapter boundary | PII detection/redaction and private models |
| Oversized upload / cost abuse | request limits | quotas, async jobs, rate limits, budgets |

Never authorize access based on model output. Retrieval authorization happens before content enters the prompt.
