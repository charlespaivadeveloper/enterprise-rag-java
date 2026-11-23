package dev.charlespaiva.rag.interfaceadapter.presenter;

import dev.charlespaiva.rag.usecase.ask.AskQuestionOutputBoundary;
import dev.charlespaiva.rag.usecase.ask.AskQuestionResponseModel;
import java.util.List;

public class AskQuestionPresenter implements AskQuestionOutputBoundary {

    private AskViewModel viewModel;

    @Override
    public void present(AskQuestionResponseModel responseModel) {
        List<AskViewModel.CitationView> citations = responseModel.citations().stream()
            .map(c -> new AskViewModel.CitationView(c.source(), c.chunk(), c.excerpt(), c.score()))
            .toList();
        viewModel = new AskViewModel(responseModel.text(), citations, responseModel.traceId());
    }

    public AskViewModel getViewModel() {
        return viewModel;
    }
}
