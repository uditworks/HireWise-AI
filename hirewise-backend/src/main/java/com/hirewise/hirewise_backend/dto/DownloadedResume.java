package com.hirewise.hirewise_backend.dto;

public class DownloadedResume {

    private final byte[] content;
    private final String filename;
    private final String contentType;

    public DownloadedResume(byte[] content, String filename, String contentType) {
        this.content = content;
        this.filename = filename;
        this.contentType = contentType != null ? contentType : "application/pdf";
    }

    public byte[] getContent() {
        return content;
    }

    public String getFilename() {
        return filename;
    }

    public String getContentType() {
        return contentType;
    }
}
