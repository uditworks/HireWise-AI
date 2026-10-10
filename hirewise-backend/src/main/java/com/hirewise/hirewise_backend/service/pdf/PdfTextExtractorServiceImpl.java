package com.hirewise.hirewise_backend.service.pdf;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class PdfTextExtractorServiceImpl implements PdfTextExtractorService {

    private static final long MAX_FILE_SIZE_BYTES = 10 * 1024 * 1024; // 10 MB
    private static final byte[] PDF_MAGIC_BYTES = new byte[]{0x25, 0x50, 0x44, 0x46, 0x2D}; // %PDF-

    @Override
    public void validatePdf(byte[] pdfBytes) {
        if (pdfBytes == null || pdfBytes.length == 0) {
            throw new IllegalArgumentException("Resume file cannot be empty");
        }

        if (pdfBytes.length > MAX_FILE_SIZE_BYTES) {
            throw new IllegalArgumentException("File size exceeds maximum allowed limit of 10 MB");
        }

        if (!hasPdfMagicBytes(pdfBytes)) {
            throw new IllegalArgumentException("Invalid file format. Only valid PDF files are accepted.");
        }

        try (PDDocument document = Loader.loadPDF(pdfBytes)) {
            if (document.isEncrypted()) {
                throw new IllegalArgumentException("Encrypted or password-protected PDF files are not supported. Please upload an unprotected PDF.");
            }
        } catch (InvalidPasswordException e) {
            throw new IllegalArgumentException("Encrypted or password-protected PDF files are not supported. Please upload an unprotected PDF.");
        } catch (IOException e) {
            throw new IllegalArgumentException("Corrupted or unreadable PDF document: " + e.getMessage());
        }
    }

    @Override
    public String extractText(byte[] pdfBytes) {
        validatePdf(pdfBytes);

        try (PDDocument document = Loader.loadPDF(pdfBytes)) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            String rawText = stripper.getText(document);
            if (rawText == null) {
                return "";
            }
            return rawText.replace("\u0000", "").trim();
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to extract text from PDF: " + e.getMessage());
        }
    }

    private boolean hasPdfMagicBytes(byte[] bytes) {
        if (bytes.length < PDF_MAGIC_BYTES.length) {
            return false;
        }
        for (int i = 0; i < PDF_MAGIC_BYTES.length; i++) {
            if (bytes[i] != PDF_MAGIC_BYTES[i]) {
                return false;
            }
        }
        return true;
    }
}
