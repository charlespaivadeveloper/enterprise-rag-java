package dev.charlespaiva.rag.usecase.ask;

public record AskQuestionRequestModel(String tenantId, String question, int topK) {
}
