package com.hirewise.hirewise_backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "resumes",
        indexes = {
                @Index(name = "idx_resumes_owner_created", columnList = "owner_id, created_at")
        }
)
public class Resume {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(name = "original_filename", nullable = false, length = 255)
    private String originalFilename;

    @Column(name = "storage_identifier", nullable = false, unique = true, length = 100)
    private String storageIdentifier;

    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @Column(name = "content_type", nullable = false, length = 50)
    private String contentType;

    @Column(name = "extracted_text", columnDefinition = "LONGTEXT")
    private String extractedText;

    @Enumerated(EnumType.STRING)
    @Column(name = "processing_status", nullable = false, length = 30)
    private ResumeProcessingStatus processingStatus = ResumeProcessingStatus.UPLOADED;

    @Column(name = "failure_reason", length = 500)
    private String failureReason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public Resume() {
    }

    public Resume(Long id, User owner, String originalFilename, String storageIdentifier,
                  Long fileSize, String contentType, String extractedText,
                  ResumeProcessingStatus processingStatus, String failureReason,
                  LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.owner = owner;
        this.originalFilename = originalFilename;
        this.storageIdentifier = storageIdentifier;
        this.fileSize = fileSize;
        this.contentType = contentType;
        this.extractedText = extractedText;
        this.processingStatus = processingStatus != null ? processingStatus : ResumeProcessingStatus.UPLOADED;
        this.failureReason = failureReason;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Resume(User owner, String originalFilename, String storageIdentifier,
                  Long fileSize, String contentType) {
        this.owner = owner;
        this.originalFilename = originalFilename;
        this.storageIdentifier = storageIdentifier;
        this.fileSize = fileSize;
        this.contentType = contentType;
        this.processingStatus = ResumeProcessingStatus.UPLOADED;
    }

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (this.createdAt == null) {
            this.createdAt = now;
        }
        if (this.updatedAt == null) {
            this.updatedAt = now;
        }
        if (this.processingStatus == null) {
            this.processingStatus = ResumeProcessingStatus.UPLOADED;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public void setOriginalFilename(String originalFilename) {
        this.originalFilename = originalFilename;
    }

    public String getStorageIdentifier() {
        return storageIdentifier;
    }

    public void setStorageIdentifier(String storageIdentifier) {
        this.storageIdentifier = storageIdentifier;
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

    public String getExtractedText() {
        return extractedText;
    }

    public void setExtractedText(String extractedText) {
        this.extractedText = extractedText;
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
