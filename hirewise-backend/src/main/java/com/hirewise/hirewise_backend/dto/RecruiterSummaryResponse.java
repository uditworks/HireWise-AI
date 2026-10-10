package com.hirewise.hirewise_backend.dto;

import com.hirewise.hirewise_backend.entity.User;

public class RecruiterSummaryResponse {

    private Long id;
    private String fullName;
    private String email;

    public RecruiterSummaryResponse() {
    }

    public RecruiterSummaryResponse(Long id, String fullName, String email) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
    }

    public static RecruiterSummaryResponse fromEntity(User recruiter) {
        if (recruiter == null) {
            return null;
        }
        return new RecruiterSummaryResponse(
                recruiter.getId(),
                recruiter.getFullName(),
                recruiter.getEmail()
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
