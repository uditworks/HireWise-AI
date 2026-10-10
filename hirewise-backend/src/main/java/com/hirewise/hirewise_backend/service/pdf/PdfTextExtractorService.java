package com.hirewise.hirewise_backend.service.pdf;

public interface PdfTextExtractorService {

    void validatePdf(byte[] pdfBytes);

    String extractText(byte[] pdfBytes);
}
