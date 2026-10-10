package com.hirewise.hirewise_backend.controller;

import com.hirewise.hirewise_backend.dto.JobCreateRequest;
import com.hirewise.hirewise_backend.dto.JobResponse;
import com.hirewise.hirewise_backend.dto.JobStatusUpdateRequest;
import com.hirewise.hirewise_backend.dto.JobUpdateRequest;
import com.hirewise.hirewise_backend.entity.EmploymentType;
import com.hirewise.hirewise_backend.service.JobService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/jobs")
public class JobController {

    private final JobService jobService;

    public JobController(JobService jobService) {
        this.jobService = jobService;
    }

    @GetMapping
    public ResponseEntity<Page<JobResponse>> getPublishedJobs(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) EmploymentType employmentType,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<JobResponse> jobs = jobService.getPublishedJobs(keyword, location, employmentType, pageable);
        return ResponseEntity.ok(jobs);
    }

    @GetMapping("/{id}")
    public ResponseEntity<JobResponse> getJobById(
            @PathVariable Long id,
            Authentication authentication) {

        String currentUserEmail = (authentication != null && authentication.isAuthenticated())
                ? authentication.getName()
                : null;

        JobResponse job = jobService.getJobById(id, currentUserEmail);
        return ResponseEntity.ok(job);
    }

    @PostMapping
    @PreAuthorize("hasRole('RECRUITER')")
    public ResponseEntity<JobResponse> createJob(
            @Valid @RequestBody JobCreateRequest request,
            Authentication authentication) {

        JobResponse createdJob = jobService.createJob(request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(createdJob);
    }

    @GetMapping("/my-jobs")
    @PreAuthorize("hasRole('RECRUITER')")
    public ResponseEntity<Page<JobResponse>> getMyJobs(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            Authentication authentication) {

        Page<JobResponse> jobs = jobService.getMyJobs(authentication.getName(), pageable);
        return ResponseEntity.ok(jobs);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('RECRUITER')")
    public ResponseEntity<JobResponse> updateJob(
            @PathVariable Long id,
            @Valid @RequestBody JobUpdateRequest request,
            Authentication authentication) {

        JobResponse updatedJob = jobService.updateJob(id, request, authentication.getName());
        return ResponseEntity.ok(updatedJob);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('RECRUITER')")
    public ResponseEntity<JobResponse> updateJobStatus(
            @PathVariable Long id,
            @Valid @RequestBody JobStatusUpdateRequest request,
            Authentication authentication) {

        JobResponse updatedJob = jobService.updateJobStatus(id, request.getStatus(), authentication.getName());
        return ResponseEntity.ok(updatedJob);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('RECRUITER')")
    public ResponseEntity<Void> deleteJob(
            @PathVariable Long id,
            Authentication authentication) {

        jobService.deleteJob(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
