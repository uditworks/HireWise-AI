package com.hirewise.hirewise_backend.dto;

import com.hirewise.hirewise_backend.entity.ApplicationStatus;
import com.hirewise.hirewise_backend.entity.JobApplication;

import java.time.LocalDateTime;

public class JobApplicationResponse {

    private Long id;
    private JobSummaryResponse job;
    private ApplicantSummaryResponse applicant;
    private ApplicationStatus status;
    private ResumeSummaryResponse resume;
    private String coverLetter;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public JobApplicationResponse() {
    }

    public JobApplicationResponse(Long id, JobSummaryResponse job, ApplicantSummaryResponse applicant,
                                  ApplicationStatus status, String coverLetter,
                                  LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.job = job;
        this.applicant = applicant;
        this.status = status;
        this.coverLetter = coverLetter;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public JobApplicationResponse(Long id, JobSummaryResponse job, ApplicantSummaryResponse applicant,
                                  ApplicationStatus status, ResumeSummaryResponse resume, String coverLetter,
                                  LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.job = job;
        this.applicant = applicant;
        this.status = status;
        this.resume = resume;
        this.coverLetter = coverLetter;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static JobApplicationResponse fromEntity(JobApplication application) {
        if (application == null) {
            return null;
        }
        ResumeSummaryResponse resumeSummary = null;
        if (application.getResume() != null) {
            resumeSummary = ResumeSummaryResponse.fromEntity(application.getResume());
        }

        return new JobApplicationResponse(
                application.getId(),
                JobSummaryResponse.fromEntity(application.getJob()),
                ApplicantSummaryResponse.fromEntity(application.getApplicant()),
                application.getStatus(),
                resumeSummary,
                application.getCoverLetter(),
                application.getCreatedAt(),
                application.getUpdatedAt()
        );
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public JobSummaryResponse getJob() {
        return job;
    }

    public void setJob(JobSummaryResponse job) {
        this.job = job;
    }

    public ApplicantSummaryResponse getApplicant() {
        return applicant;
    }

    public void setApplicant(ApplicantSummaryResponse applicant) {
        this.applicant = applicant;
    }

    public ApplicationStatus getStatus() {
        return status;
    }

    public void setStatus(ApplicationStatus status) {
        this.status = status;
    }

    public ResumeSummaryResponse getResume() {
        return resume;
    }

    public void setResume(ResumeSummaryResponse resume) {
        this.resume = resume;
    }

    public String getCoverLetter() {
        return coverLetter;
    }

    public void setCoverLetter(String coverLetter) {
        this.coverLetter = coverLetter;
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
