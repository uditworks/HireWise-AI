package com.hirewise.hirewise_backend.dto;

import com.hirewise.hirewise_backend.entity.EmploymentType;
import com.hirewise.hirewise_backend.entity.Job;
import com.hirewise.hirewise_backend.entity.JobStatus;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class JobResponse {

    private Long id;
    private String title;
    private String description;
    private String location;
    private EmploymentType employmentType;
    private String salaryRange;
    private Set<String> requiredSkills;
    private JobStatus status;
    private RecruiterSummaryResponse recruiter;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public JobResponse() {
    }

    public JobResponse(Long id, String title, String description, String location,
                       EmploymentType employmentType, String salaryRange,
                       Set<String> requiredSkills, JobStatus status,
                       RecruiterSummaryResponse recruiter,
                       LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.location = location;
        this.employmentType = employmentType;
        this.salaryRange = salaryRange;
        this.requiredSkills = requiredSkills != null ? requiredSkills : Collections.emptySet();
        this.status = status;
        this.recruiter = recruiter;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static JobResponse fromEntity(Job job) {
        if (job == null) {
            return null;
        }
        return new JobResponse(
                job.getId(),
                job.getTitle(),
                job.getDescription(),
                job.getLocation(),
                job.getEmploymentType(),
                job.getSalaryRange(),
                job.getRequiredSkills() != null ? new HashSet<>(job.getRequiredSkills()) : Collections.emptySet(),
                job.getStatus(),
                RecruiterSummaryResponse.fromEntity(job.getRecruiter()),
                job.getCreatedAt(),
                job.getUpdatedAt()
        );
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public EmploymentType getEmploymentType() {
        return employmentType;
    }

    public void setEmploymentType(EmploymentType employmentType) {
        this.employmentType = employmentType;
    }

    public String getSalaryRange() {
        return salaryRange;
    }

    public void setSalaryRange(String salaryRange) {
        this.salaryRange = salaryRange;
    }

    public Set<String> getRequiredSkills() {
        return requiredSkills;
    }

    public void setRequiredSkills(Set<String> requiredSkills) {
        this.requiredSkills = requiredSkills != null ? requiredSkills : Collections.emptySet();
    }

    public JobStatus getStatus() {
        return status;
    }

    public void setStatus(JobStatus status) {
        this.status = status;
    }

    public RecruiterSummaryResponse getRecruiter() {
        return recruiter;
    }

    public void setRecruiter(RecruiterSummaryResponse recruiter) {
        this.recruiter = recruiter;
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
