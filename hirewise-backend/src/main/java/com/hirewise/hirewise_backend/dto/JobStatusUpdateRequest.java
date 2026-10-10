package com.hirewise.hirewise_backend.dto;

import com.hirewise.hirewise_backend.entity.JobStatus;
import jakarta.validation.constraints.NotNull;

public class JobStatusUpdateRequest {

    @NotNull(message = "Status is required")
    private JobStatus status;

    public JobStatusUpdateRequest() {
    }

    public JobStatusUpdateRequest(JobStatus status) {
        this.status = status;
    }

    public JobStatus getStatus() {
        return status;
    }

    public void setStatus(JobStatus status) {
        this.status = status;
    }
}
