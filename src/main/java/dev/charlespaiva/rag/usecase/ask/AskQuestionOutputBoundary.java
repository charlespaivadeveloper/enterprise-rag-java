package dev.charlespaiva.rag.usecase.ask;

@FunctionalInterface
public interface AskQuestionOutputBoundary {
    void present(AskQuestionResponseModel responseModel);
}
