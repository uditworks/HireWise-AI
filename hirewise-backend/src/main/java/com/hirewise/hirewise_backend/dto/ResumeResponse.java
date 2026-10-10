package com.hirewise.hirewise_backend.dto;

import com.hirewise.hirewise_backend.entity.Resume;
import com.hirewise.hirewise_backend.entity.ResumeProcessingStatus;

import java.time.LocalDateTime;

public class ResumeResponse {

    private Long id;
    private Long ownerId;
    private String ownerEmail;
    private String originalFilename;
    private Long fileSize;
    private String contentType;
    private ResumeProcessingStatus processingStatus;
    private String failureReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ResumeResponse() {
    }

    public ResumeResponse(Long id, Long ownerId, String ownerEmail, String originalFilename,
                          Long fileSize, String contentType, ResumeProcessingStatus processingStatus,
                          String failureReason, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.ownerId = ownerId;
        this.ownerEmail = ownerEmail;
        this.originalFilename = originalFilename;
        this.fileSize = fileSize;
        this.contentType = contentType;
        this.processingStatus = processingStatus;
        this.failureReason = failureReason;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static ResumeResponse fromEntity(Resume resume) {
        if (resume == null) {
            return null;
        }
        return new ResumeResponse(
                resume.getId(),
                resume.getOwner() != null ? resume.getOwner().getId() : null,
                resume.getOwner() != null ? resume.getOwner().getEmail() : null,
                resume.getOriginalFilename(),
                resume.getFileSize(),
                resume.getContentType(),
                resume.getProcessingStatus(),
                resume.getFailureReason(),
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

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    public String getOwnerEmail() {
        return ownerEmail;
    }

    public void setOwnerEmail(String ownerEmail) {
        this.ownerEmail = ownerEmail;
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

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public ResumeProcessingStatus getProcessingStatus() {
        return processingStatus;
    }

    public void setProcessingStatus(ResumeProcessingStatus processingStatus) {
        this.processingStatus = processingStatus;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
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
