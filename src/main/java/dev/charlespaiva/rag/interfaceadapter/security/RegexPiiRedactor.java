package dev.charlespaiva.rag.interfaceadapter.security;

import dev.charlespaiva.rag.usecase.ingest.PiiRedactorPort;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import java.util.regex.Pattern;

@Component
@ConditionalOnProperty(name = "rag.pii.redaction.enabled", havingValue = "true")
public class RegexPiiRedactor implements PiiRedactorPort {

    private static final Pattern EMAIL =
        Pattern.compile("[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}");
    private static final Pattern PHONE =
        Pattern.compile("\\b(?:\\+?1[-.\\s]?)?\\(?\\d{3}\\)?[-.\\s]?\\d{3}[-.\\s]?\\d{4}\\b");
    private static final Pattern SSN =
        Pattern.compile("\\b\\d{3}-\\d{2}-\\d{4}\\b");
    private static final Pattern CREDIT_CARD =
        Pattern.compile("\\b(?:4[0-9]{12}(?:[0-9]{3})?|5[1-5][0-9]{14}|3[47][0-9]{13})\\b");

    @Override
    public String redact(String text) {
        String result = EMAIL.matcher(text).replaceAll("[EMAIL]");
        result = PHONE.matcher(result).replaceAll("[PHONE]");
        result = SSN.matcher(result).replaceAll("[SSN]");
        return CREDIT_CARD.matcher(result).replaceAll("[CREDIT_CARD]");
    }
}
