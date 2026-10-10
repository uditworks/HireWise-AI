package com.hirewise.hirewise_backend.service;

import com.hirewise.hirewise_backend.dto.JobCreateRequest;
import com.hirewise.hirewise_backend.dto.JobResponse;
import com.hirewise.hirewise_backend.dto.JobUpdateRequest;
import com.hirewise.hirewise_backend.entity.EmploymentType;
import com.hirewise.hirewise_backend.entity.JobStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface JobService {

    Page<JobResponse> getPublishedJobs(String keyword, String location, EmploymentType employmentType, Pageable pageable);

    JobResponse getJobById(Long id, String currentUserEmail);

    JobResponse createJob(JobCreateRequest request, String recruiterEmail);

    Page<JobResponse> getMyJobs(String recruiterEmail, Pageable pageable);

    JobResponse updateJob(Long id, JobUpdateRequest request, String recruiterEmail);

    JobResponse updateJobStatus(Long id, JobStatus status, String recruiterEmail);

    void deleteJob(Long id, String recruiterEmail);
}
