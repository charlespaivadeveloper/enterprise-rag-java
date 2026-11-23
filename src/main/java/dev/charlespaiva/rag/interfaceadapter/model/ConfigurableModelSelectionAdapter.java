package dev.charlespaiva.rag.interfaceadapter.model;

import dev.charlespaiva.rag.usecase.model.ModelConfig;
import dev.charlespaiva.rag.usecase.model.ModelSelectionPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ConfigurableModelSelectionAdapter implements ModelSelectionPort {

    private final String chatModel;
    private final String embeddingModel;

    public ConfigurableModelSelectionAdapter(
            @Value("${rag.model.chat.model:gpt-4.1-mini}") String chatModel,
            @Value("${rag.model.embedding.model:text-embedding-3-small}") String embeddingModel) {
        this.chatModel = chatModel;
        this.embeddingModel = embeddingModel;
    }

    @Override
    public ModelConfig select(String tenantId) {
        return new ModelConfig(chatModel, embeddingModel);
    }
}
