package com.hirewise.hirewise_backend.service.pdf;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.pdmodel.encryption.StandardProtectionPolicy;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PdfTextExtractorServiceTest {

    private PdfTextExtractorServiceImpl pdfTextExtractorService;

    @BeforeEach
    void setUp() {
        pdfTextExtractorService = new PdfTextExtractorServiceImpl();
    }

    private byte[] createTestPdf(String text) throws IOException {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);

            if (text != null && !text.isBlank()) {
                try (PDPageContentStream stream = new PDPageContentStream(document, page)) {
                    stream.beginText();
                    stream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                    stream.newLineAtOffset(50, 700);
                    String[] lines = text.split("\\r?\\n");
                    for (int i = 0; i < lines.length; i++) {
                        stream.showText(lines[i]);
                        if (i < lines.length - 1) {
                            stream.newLineAtOffset(0, -15);
                        }
                    }
                    stream.endText();
                }
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            document.save(baos);
            return baos.toByteArray();
        }
    }

    private byte[] createEncryptedPdf(String text, String password) throws IOException {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);

            try (PDPageContentStream stream = new PDPageContentStream(document, page)) {
                stream.beginText();
                stream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                stream.newLineAtOffset(50, 700);
                String safeText = text != null ? text : "Encrypted Content";
                String[] lines = safeText.split("\\r?\\n");
                for (int i = 0; i < lines.length; i++) {
                    stream.showText(lines[i]);
                    if (i < lines.length - 1) {
                        stream.newLineAtOffset(0, -15);
                    }
                }
                stream.endText();
            }

            AccessPermission ap = new AccessPermission();
            StandardProtectionPolicy spp = new StandardProtectionPolicy(password, password, ap);
            document.protect(spp);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            document.save(baos);
            return baos.toByteArray();
        }
    }

    @Test
    @DisplayName("Should successfully validate and extract text from valid PDF")
    void testExtractTextFromValidPdf() throws IOException {
        byte[] pdfBytes = createTestPdf("John Doe - Senior Java Engineer with Spring Boot experience");

        String extracted = pdfTextExtractorService.extractText(pdfBytes);

        assertThat(extracted).isNotBlank();
        assertThat(extracted).contains("John Doe");
        assertThat(extracted).contains("Senior Java Engineer");
    }

    @Test
    @DisplayName("Should reject null or empty byte array")
    void testValidateEmptyPdf() {
        assertThatThrownBy(() -> pdfTextExtractorService.validatePdf(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cannot be empty");

        assertThatThrownBy(() -> pdfTextExtractorService.validatePdf(new byte[0]))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cannot be empty");
    }

    @Test
    @DisplayName("Should reject non-PDF files missing magic bytes")
    void testRejectNonPdfFiles() {
        byte[] textFile = "This is just a plain text file, not a real PDF".getBytes();

        assertThatThrownBy(() -> pdfTextExtractorService.validatePdf(textFile))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid file format");
    }

    @Test
    @DisplayName("Should reject password-protected or encrypted PDFs")
    void testRejectEncryptedPdf() throws IOException {
        byte[] encryptedPdf = createEncryptedPdf("Top secret candidate resume", "secret123");

        assertThatThrownBy(() -> pdfTextExtractorService.validatePdf(encryptedPdf))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Encrypted or password-protected PDF files are not supported");
    }

    @Test
    @DisplayName("Should handle text-free/scanned PDFs gracefully without throwing")
    void testHandleScannedPdfGracefully() throws IOException {
        byte[] emptyPagePdf = createTestPdf(null);

        String text = pdfTextExtractorService.extractText(emptyPagePdf);

        assertThat(text).isNotNull();
        assertThat(text).isBlank();
    }

    @Test
    @DisplayName("Should reject corrupted PDF with valid magic bytes but malformed body")
    void testRejectCorruptedPdf() {
        byte[] corrupted = new byte[]{'%', 'P', 'D', 'F', '-', '1', '.', '4', 'X', 'Y', 'Z', '9', '9'};

        assertThatThrownBy(() -> pdfTextExtractorService.validatePdf(corrupted))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Corrupted or unreadable PDF document");
    }
}
