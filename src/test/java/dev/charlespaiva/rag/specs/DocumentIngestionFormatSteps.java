package dev.charlespaiva.rag.specs;

import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class DocumentIngestionFormatSteps {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private JdbcTemplate jdbc;

    private int lastStatus;

    @Before
    public void cleanDatabase() {
        jdbc.execute("TRUNCATE knowledge_chunk");
    }

    @Given("tenant {string} uploads a PDF file {string} with text {string}")
    public void uploadPdf(String tenant, String sourceName, String text) throws IOException {
        byte[] pdfBytes = buildPdf(text);
        postFile(tenant, sourceName, pdfBytes, "application/pdf");
    }

    @Given("tenant {string} uploads a DOCX file {string} with text {string}")
    public void uploadDocx(String tenant, String sourceName, String text) throws IOException {
        byte[] docxBytes = buildDocx(text);
        postFile(tenant, sourceName, docxBytes,
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
    }

    @Given("tenant {string} uploads a file {string} with content type {string} and bytes {string}")
    public void uploadUnsupported(String tenant, String sourceName, String contentType, String content) {
        byte[] bytes = content.getBytes();
        ResponseEntity<Map> response = postFileRaw(tenant, sourceName, bytes, contentType);
        lastStatus = response.getStatusCode().value();
    }

    @Then("the ingestion response status is {int}")
    public void ingestionResponseStatus(int expected) {
        assertThat(lastStatus).isEqualTo(expected);
    }

    private void postFile(String tenant, String sourceName, byte[] bytes, String contentType) {
        ResponseEntity<Map> response = postFileRaw(tenant, sourceName, bytes, contentType);
        lastStatus = response.getStatusCode().value();
    }

    @SuppressWarnings("unchecked")
    private ResponseEntity<Map> postFileRaw(String tenant, String sourceName, byte[] bytes, String contentType) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Tenant-Id", tenant);
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("sourceName", sourceName);
        ByteArrayResource resource = new ByteArrayResource(bytes) {
            @Override
            public String getFilename() {
                return sourceName;
            }
        };
        HttpHeaders fileHeaders = new HttpHeaders();
        fileHeaders.setContentType(MediaType.parseMediaType(contentType));
        body.add("file", new HttpEntity<>(resource, fileHeaders));
        return restTemplate.exchange("/api/v1/documents/upload", HttpMethod.POST,
            new HttpEntity<>(body, headers), Map.class);
    }

    private byte[] buildPdf(String text) throws IOException {
        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage();
            doc.addPage(page);
            try (PDPageContentStream stream = new PDPageContentStream(doc, page)) {
                stream.beginText();
                stream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                stream.newLineAtOffset(50, 700);
                stream.showText(text);
                stream.endText();
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            doc.save(out);
            return out.toByteArray();
        }
    }

    private byte[] buildDocx(String text) throws IOException {
        try (XWPFDocument doc = new XWPFDocument()) {
            doc.createParagraph().createRun().setText(text);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            doc.write(out);
            return out.toByteArray();
        }
    }
}
