package com.hirewise.hirewise_backend.dto;

import jakarta.validation.constraints.Size;

public class JobApplicationCreateRequest {

    private Long resumeId;

    @Size(max = 5000, message = "Cover letter must not exceed 5000 characters")
    private String coverLetter;

    public JobApplicationCreateRequest() {
    }

    public JobApplicationCreateRequest(String coverLetter) {
        this.coverLetter = coverLetter;
    }

    public JobApplicationCreateRequest(Long resumeId, String coverLetter) {
        this.resumeId = resumeId;
        this.coverLetter = coverLetter;
    }

    public Long getResumeId() {
        return resumeId;
    }

    public void setResumeId(Long resumeId) {
        this.resumeId = resumeId;
    }

    public String getCoverLetter() {
        return coverLetter;
    }

    public void setCoverLetter(String coverLetter) {
        this.coverLetter = coverLetter;
    }
}
