package dev.charlespaiva.rag.interfaceadapter.presenter;

import dev.charlespaiva.rag.usecase.ingest.SubmitIngestJobOutputBoundary;
import dev.charlespaiva.rag.usecase.ingest.SubmitIngestJobResponseModel;

public class SubmitIngestJobPresenter implements SubmitIngestJobOutputBoundary {

    private SubmitIngestJobResponseModel responseModel;

    @Override
    public void present(SubmitIngestJobResponseModel response) {
        this.responseModel = response;
    }

    public SubmitIngestJobResponseModel getResponseModel() {
        return responseModel;
    }
}
