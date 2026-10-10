package com.hirewise.hirewise_backend.service;

import com.hirewise.hirewise_backend.dto.JobApplicationCreateRequest;
import com.hirewise.hirewise_backend.dto.JobApplicationResponse;
import com.hirewise.hirewise_backend.entity.ApplicationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface JobApplicationService {

    JobApplicationResponse applyToJob(Long jobId, JobApplicationCreateRequest request, String applicantEmail);

    Page<JobApplicationResponse> getMyApplications(String applicantEmail, Pageable pageable);

    JobApplicationResponse getApplicationById(Long id, String currentUserEmail);

    JobApplicationResponse withdrawApplication(Long id, String applicantEmail);

    Page<JobApplicationResponse> getApplicationsForJob(Long jobId, ApplicationStatus status, String recruiterEmail, Pageable pageable);

    JobApplicationResponse updateApplicationStatus(Long id, ApplicationStatus newStatus, String recruiterEmail);
}
