package dev.charlespaiva.rag.interfaceadapter.presenter;

import dev.charlespaiva.rag.usecase.ingest.GetIngestJobStatusOutputBoundary;
import dev.charlespaiva.rag.usecase.ingest.GetIngestJobStatusResponseModel;
import java.util.Optional;

public class GetIngestJobStatusPresenter implements GetIngestJobStatusOutputBoundary {

    private GetIngestJobStatusResponseModel responseModel;
    private boolean found = true;

    @Override
    public void present(GetIngestJobStatusResponseModel response) {
        this.responseModel = response;
    }

    @Override
    public void notFound() {
        this.found = false;
    }

    public Optional<GetIngestJobStatusResponseModel> getResponseModel() {
        return Optional.ofNullable(responseModel);
    }

    public boolean wasFound() {
        return found;
    }
}
