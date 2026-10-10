package com.hirewise.hirewise_backend.dto;

import com.hirewise.hirewise_backend.entity.EmploymentType;
import com.hirewise.hirewise_backend.entity.Job;
import com.hirewise.hirewise_backend.entity.JobStatus;

public class JobSummaryResponse {

    private Long id;
    private String title;
    private String location;
    private EmploymentType employmentType;
    private JobStatus status;

    public JobSummaryResponse() {
    }

    public JobSummaryResponse(Long id, String title, String location,
                              EmploymentType employmentType, JobStatus status) {
        this.id = id;
        this.title = title;
        this.location = location;
        this.employmentType = employmentType;
        this.status = status;
    }

    public static JobSummaryResponse fromEntity(Job job) {
        if (job == null) {
            return null;
        }
        return new JobSummaryResponse(
                job.getId(),
                job.getTitle(),
                job.getLocation(),
                job.getEmploymentType(),
                job.getStatus()
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

    public JobStatus getStatus() {
        return status;
    }

    public void setStatus(JobStatus status) {
        this.status = status;
    }
}
