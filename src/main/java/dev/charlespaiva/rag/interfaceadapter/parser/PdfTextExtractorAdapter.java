package dev.charlespaiva.rag.interfaceadapter.parser;

import dev.charlespaiva.rag.usecase.ingest.DocumentTextExtractorPort;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;
import java.io.IOException;

@Component
public class PdfTextExtractorAdapter implements DocumentTextExtractorPort {

    @Override
    public boolean supports(String mediaType) {
        return "application/pdf".equals(mediaType);
    }

    @Override
    public String extract(byte[] content, String mediaType) {
        try (PDDocument doc = Loader.loadPDF(content)) {
            return new PDFTextStripper().getText(doc);
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to extract text from PDF: " + e.getMessage(), e);
        }
    }
}
