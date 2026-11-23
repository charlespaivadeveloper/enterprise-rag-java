package dev.charlespaiva.rag.interfaceadapter.parser;

import dev.charlespaiva.rag.usecase.ingest.DocumentTextExtractorPort;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class CompositeDocumentTextExtractor {

    private final List<DocumentTextExtractorPort> extractors;

    public CompositeDocumentTextExtractor(List<DocumentTextExtractorPort> extractors) {
        this.extractors = List.copyOf(extractors);
    }

    public String extract(byte[] content, String mediaType) {
        return extractors.stream()
            .filter(e -> e.supports(mediaType))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Unsupported media type: " + mediaType))
            .extract(content, mediaType);
    }
}
