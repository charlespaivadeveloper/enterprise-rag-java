package dev.charlespaiva.rag.interfaceadapter.parser;

import dev.charlespaiva.rag.usecase.ingest.DocumentTextExtractorPort;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Component;
import java.io.ByteArrayInputStream;
import java.io.IOException;

@Component
public class DocxTextExtractorAdapter implements DocumentTextExtractorPort {

    @Override
    public boolean supports(String mediaType) {
        return "application/vnd.openxmlformats-officedocument.wordprocessingml.document".equals(mediaType);
    }

    @Override
    public String extract(byte[] content, String mediaType) {
        try (XWPFDocument doc = new XWPFDocument(new ByteArrayInputStream(content))) {
            StringBuilder sb = new StringBuilder();
            doc.getParagraphs().forEach(p -> sb.append(p.getText()).append('\n'));
            return sb.toString();
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to extract text from DOCX: " + e.getMessage(), e);
        }
    }
}
