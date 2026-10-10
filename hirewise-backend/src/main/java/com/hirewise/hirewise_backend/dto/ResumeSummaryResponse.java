package com.hirewise.hirewise_backend.dto;

import com.hirewise.hirewise_backend.entity.Resume;
import com.hirewise.hirewise_backend.entity.ResumeProcessingStatus;

import java.time.LocalDateTime;

public class ResumeSummaryResponse {

    private Long id;
    private String originalFilename;
    private Long fileSize;
    private ResumeProcessingStatus processingStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ResumeSummaryResponse() {
    }

    public ResumeSummaryResponse(Long id, String originalFilename, Long fileSize,
                                 ResumeProcessingStatus processingStatus,
                                 LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.originalFilename = originalFilename;
        this.fileSize = fileSize;
        this.processingStatus = processingStatus;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static ResumeSummaryResponse fromEntity(Resume resume) {
        if (resume == null) {
            return null;
        }
        return new ResumeSummaryResponse(
                resume.getId(),
                resume.getOriginalFilename(),
                resume.getFileSize(),
                resume.getProcessingStatus(),
                resume.getCreatedAt(),
                resume.getUpdatedAt()
        );
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public void setOriginalFilename(String originalFilename) {
        this.originalFilename = originalFilename;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public ResumeProcessingStatus getProcessingStatus() {
        return processingStatus;
    }

    public void setProcessingStatus(ResumeProcessingStatus processingStatus) {
        this.processingStatus = processingStatus;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
