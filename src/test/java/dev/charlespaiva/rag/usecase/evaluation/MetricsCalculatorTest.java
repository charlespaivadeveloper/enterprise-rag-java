package dev.charlespaiva.rag.usecase.evaluation;

import org.junit.jupiter.api.Test;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class MetricsCalculatorTest {

    @Test
    void precisionAtKPerfect() {
        double p = MetricsCalculator.precisionAtK(List.of("a.pdf", "b.pdf"), List.of("a.pdf", "b.pdf"));
        assertThat(p).isCloseTo(1.0, within(0.001));
    }

    @Test
    void precisionAtKZeroWhenNoMatch() {
        double p = MetricsCalculator.precisionAtK(List.of("c.pdf"), List.of("a.pdf", "b.pdf"));
        assertThat(p).isCloseTo(0.0, within(0.001));
    }

    @Test
    void recallAtKPerfect() {
        double r = MetricsCalculator.recallAtK(List.of("a.pdf", "b.pdf", "c.pdf"), List.of("a.pdf", "b.pdf"));
        assertThat(r).isCloseTo(1.0, within(0.001));
    }

    @Test
    void recallAtKPartial() {
        double r = MetricsCalculator.recallAtK(List.of("a.pdf"), List.of("a.pdf", "b.pdf"));
        assertThat(r).isCloseTo(0.5, within(0.001));
    }

    @Test
    void reciprocalRankFirstHit() {
        double rr = MetricsCalculator.reciprocalRank(List.of("a.pdf", "b.pdf"), List.of("a.pdf"));
        assertThat(rr).isCloseTo(1.0, within(0.001));
    }

    @Test
    void reciprocalRankSecondHit() {
        double rr = MetricsCalculator.reciprocalRank(List.of("c.pdf", "a.pdf"), List.of("a.pdf"));
        assertThat(rr).isCloseTo(0.5, within(0.001));
    }

    @Test
    void reciprocalRankNoHit() {
        double rr = MetricsCalculator.reciprocalRank(List.of("c.pdf"), List.of("a.pdf"));
        assertThat(rr).isCloseTo(0.0, within(0.001));
    }

    @Test
    void ndcgPerfect() {
        double ndcg = MetricsCalculator.ndcgAtK(List.of("a.pdf", "b.pdf"), List.of("a.pdf", "b.pdf"));
        assertThat(ndcg).isCloseTo(1.0, within(0.001));
    }

    @Test
    void ndcgEmptyRelevant() {
        double ndcg = MetricsCalculator.ndcgAtK(List.of("a.pdf"), List.of());
        assertThat(ndcg).isCloseTo(0.0, within(0.001));
    }
}
