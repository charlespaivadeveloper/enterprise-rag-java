package dev.charlespaiva.rag.interfaceadapter.security;

import dev.charlespaiva.rag.usecase.ingest.PiiRedactorPort;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "rag.pii.redaction.enabled", havingValue = "false", matchIfMissing = true)
public class NoOpPiiRedactor implements PiiRedactorPort {

    @Override
    public String redact(String text) {
        return text;
    }
}
