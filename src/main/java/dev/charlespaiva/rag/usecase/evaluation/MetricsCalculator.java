package dev.charlespaiva.rag.usecase.evaluation;

import java.util.List;

public final class MetricsCalculator {

    private MetricsCalculator() {
    }

    public static double precisionAtK(List<String> retrieved, List<String> relevant) {
        if (retrieved.isEmpty()) {
            return 0.0;
        }
        long matches = retrieved.stream().filter(relevant::contains).count();
        return (double) matches / retrieved.size();
    }

    public static double recallAtK(List<String> retrieved, List<String> relevant) {
        if (relevant.isEmpty()) {
            return 1.0;
        }
        long matches = retrieved.stream().filter(relevant::contains).count();
        return (double) matches / relevant.size();
    }

    public static double reciprocalRank(List<String> retrieved, List<String> relevant) {
        for (int i = 0; i < retrieved.size(); i++) {
            if (relevant.contains(retrieved.get(i))) {
                return 1.0 / (i + 1);
            }
        }
        return 0.0;
    }

    public static double ndcgAtK(List<String> retrieved, List<String> relevant) {
        double dcg = 0.0;
        for (int i = 0; i < retrieved.size(); i++) {
            if (relevant.contains(retrieved.get(i))) {
                dcg += 1.0 / (Math.log(i + 2) / Math.log(2));
            }
        }
        double idcg = 0.0;
        int idealCount = Math.min(relevant.size(), retrieved.size());
        for (int i = 0; i < idealCount; i++) {
            idcg += 1.0 / (Math.log(i + 2) / Math.log(2));
        }
        return idcg == 0 ? 0.0 : dcg / idcg;
    }
}
