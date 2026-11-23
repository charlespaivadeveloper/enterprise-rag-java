# Evaluation strategy

Track retrieval recall@k, MRR/nDCG, groundedness, answer relevance, citation precision/recall, refusal correctness, latency and cost. A versioned golden dataset must include normal questions, unanswerable questions, conflicting documents, tenant-leak attempts and prompt-injection samples. CI in v0.2 will enforce quality thresholds and detect regressions.
