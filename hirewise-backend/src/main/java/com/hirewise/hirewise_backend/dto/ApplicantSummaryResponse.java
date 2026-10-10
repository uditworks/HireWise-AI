package com.hirewise.hirewise_backend.dto;

import com.hirewise.hirewise_backend.entity.User;

public class ApplicantSummaryResponse {

    private Long id;
    private String fullName;
    private String email;

    public ApplicantSummaryResponse() {
    }

    public ApplicantSummaryResponse(Long id, String fullName, String email) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
    }

    public static ApplicantSummaryResponse fromEntity(User applicant) {
        if (applicant == null) {
            return null;
        }
        return new ApplicantSummaryResponse(
                applicant.getId(),
                applicant.getFullName(),
                applicant.getEmail()
        );
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
