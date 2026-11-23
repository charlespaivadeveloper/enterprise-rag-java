package dev.charlespaiva.rag.usecase.ingest;

@FunctionalInterface
public interface PiiRedactorPort {
    String redact(String text);
}
