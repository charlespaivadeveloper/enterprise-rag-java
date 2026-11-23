package dev.charlespaiva.rag.usecase.common;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DocumentChunkerTest {

    @Test
    void shouldSplitWithOverlap() {
        var chunks = new DocumentChunker().split("a ".repeat(700), 200, 30);
        assertThat(chunks).hasSizeGreaterThan(1).allMatch(s -> !s.isBlank());
    }

    @Test
    void shouldRejectInvalidConfig() {
        assertThatThrownBy(() -> new DocumentChunker().split("x", 100, 100))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
