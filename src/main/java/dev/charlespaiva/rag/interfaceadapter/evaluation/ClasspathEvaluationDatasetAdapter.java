package dev.charlespaiva.rag.interfaceadapter.evaluation;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.charlespaiva.rag.usecase.evaluation.EvaluationDatasetPort;
import dev.charlespaiva.rag.usecase.evaluation.EvaluationSample;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Component
public class ClasspathEvaluationDatasetAdapter implements EvaluationDatasetPort {

    private final ObjectMapper objectMapper;

    public ClasspathEvaluationDatasetAdapter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public List<EvaluationSample> load(String datasetName) {
        String path = "/evaluation/" + datasetName + ".json";
        try (InputStream is = getClass().getResourceAsStream(path)) {
            if (is == null) {
                throw new IllegalArgumentException("Evaluation dataset not found: " + datasetName);
            }
            return objectMapper.readValue(is, new TypeReference<>() { });
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load evaluation dataset: " + datasetName, e);
        }
    }
}
