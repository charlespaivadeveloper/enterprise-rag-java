package dev.charlespaiva.rag.usecase.model;

@FunctionalInterface
public interface ModelSelectionPort {
    ModelConfig select(String tenantId);
}
