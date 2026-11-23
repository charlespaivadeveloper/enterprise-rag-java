package dev.charlespaiva.rag.usecase.common;

import java.util.List;
import java.util.regex.Pattern;

public class PromptInjectionGuard {

    private static final int MAX_QUESTION_LENGTH = 4000;

    private final List<Pattern> blocked = List.of(
        Pattern.compile("(?i)ignore (all |the )?(previous|prior) instructions"),
        Pattern.compile("(?i)(reveal|print|show).{0,20}(system prompt|hidden instructions)"),
        Pattern.compile("(?i)act as (system|developer)"));

    public void validateQuery(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("question is required");
        }
        if (value.length() > MAX_QUESTION_LENGTH) {
            throw new IllegalArgumentException("question too long");
        }
        if (blocked.stream().anyMatch(p -> p.matcher(value).find())) {
            throw new SecurityException("suspected prompt injection");
        }
    }

    public String sanitizeDocument(String value) {
        return value.replaceAll("(?i)(system|assistant|developer)\\s*:", "[untrusted-role]:");
    }
}
