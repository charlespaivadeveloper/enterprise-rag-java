package dev.charlespaiva.rag.usecase.ask;

@FunctionalInterface
public interface AskQuestionInputBoundary {
    void execute(AskQuestionRequestModel requestModel, AskQuestionOutputBoundary outputBoundary);
}
