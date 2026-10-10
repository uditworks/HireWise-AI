package com.hirewise.hirewise_backend.controller;

import com.hirewise.hirewise_backend.dto.ApplicationStatusUpdateRequest;
import com.hirewise.hirewise_backend.dto.JobApplicationCreateRequest;
import com.hirewise.hirewise_backend.dto.JobApplicationResponse;
import com.hirewise.hirewise_backend.entity.ApplicationStatus;
import com.hirewise.hirewise_backend.service.JobApplicationService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class JobApplicationController {

    private final JobApplicationService jobApplicationService;

    public JobApplicationController(JobApplicationService jobApplicationService) {
        this.jobApplicationService = jobApplicationService;
    }

    @PostMapping("/jobs/{jobId}/applications")
    @PreAuthorize("hasRole('JOB_SEEKER')")
    public ResponseEntity<JobApplicationResponse> applyToJob(
            @PathVariable Long jobId,
            @Valid @RequestBody(required = false) JobApplicationCreateRequest request,
            Authentication authentication) {

        JobApplicationResponse response = jobApplicationService.applyToJob(jobId, request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/applications/my-applications")
    @PreAuthorize("hasRole('JOB_SEEKER')")
    public ResponseEntity<Page<JobApplicationResponse>> getMyApplications(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            Authentication authentication) {

        Page<JobApplicationResponse> applications = jobApplicationService.getMyApplications(authentication.getName(), pageable);
        return ResponseEntity.ok(applications);
    }

    @PatchMapping("/applications/{id}/withdraw")
    @PreAuthorize("hasRole('JOB_SEEKER')")
    public ResponseEntity<JobApplicationResponse> withdrawApplication(
            @PathVariable Long id,
            Authentication authentication) {

        JobApplicationResponse response = jobApplicationService.withdrawApplication(id, authentication.getName());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/applications/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<JobApplicationResponse> getApplicationById(
            @PathVariable Long id,
            Authentication authentication) {

        JobApplicationResponse response = jobApplicationService.getApplicationById(id, authentication.getName());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/jobs/{jobId}/applications")
    @PreAuthorize("hasRole('RECRUITER')")
    public ResponseEntity<Page<JobApplicationResponse>> getApplicationsForJob(
            @PathVariable Long jobId,
            @RequestParam(required = false) ApplicationStatus status,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            Authentication authentication) {

        Page<JobApplicationResponse> applications = jobApplicationService.getApplicationsForJob(jobId, status, authentication.getName(), pageable);
        return ResponseEntity.ok(applications);
    }

    @PatchMapping("/applications/{id}/status")
    @PreAuthorize("hasRole('RECRUITER')")
    public ResponseEntity<JobApplicationResponse> updateApplicationStatus(
            @PathVariable Long id,
            @Valid @RequestBody ApplicationStatusUpdateRequest request,
            Authentication authentication) {

        JobApplicationResponse response = jobApplicationService.updateApplicationStatus(id, request.getStatus(), authentication.getName());
        return ResponseEntity.ok(response);
    }
}
