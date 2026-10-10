package com.hirewise.hirewise_backend.service;

import com.hirewise.hirewise_backend.dto.JobCreateRequest;
import com.hirewise.hirewise_backend.dto.JobResponse;
import com.hirewise.hirewise_backend.dto.JobStatusUpdateRequest;
import com.hirewise.hirewise_backend.dto.JobUpdateRequest;
import com.hirewise.hirewise_backend.entity.EmploymentType;
import com.hirewise.hirewise_backend.entity.Job;
import com.hirewise.hirewise_backend.entity.JobStatus;
import com.hirewise.hirewise_backend.entity.Role;
import com.hirewise.hirewise_backend.entity.User;
import com.hirewise.hirewise_backend.exception.ResourceNotFoundException;
import com.hirewise.hirewise_backend.repository.JobRepository;
import com.hirewise.hirewise_backend.repository.JobSpecifications;
import com.hirewise.hirewise_backend.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class JobServiceImpl implements JobService {

    private final JobRepository jobRepository;
    private final UserRepository userRepository;

    public JobServiceImpl(JobRepository jobRepository, UserRepository userRepository) {
        this.jobRepository = jobRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<JobResponse> getPublishedJobs(String keyword, String location,
                                             EmploymentType employmentType, Pageable pageable) {
        Specification<Job> spec = JobSpecifications.filterJobs(
                JobStatus.PUBLISHED,
                keyword,
                location,
                employmentType
        );
        return jobRepository.findAll(spec, pageable).map(JobResponse::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public JobResponse getJobById(Long id, String currentUserEmail) {
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with id: " + id));

        // Public and unauthorized users can only see PUBLISHED jobs.
        // Return 404 for DRAFT or CLOSED jobs if the caller is not the owning recruiter.
        if (job.getStatus() != JobStatus.PUBLISHED) {
            boolean isOwner = currentUserEmail != null &&
                    job.getRecruiter().getEmail().equalsIgnoreCase(currentUserEmail.trim());
            if (!isOwner) {
                throw new ResourceNotFoundException("Job not found with id: " + id);
            }
        }

        return JobResponse.fromEntity(job);
    }

    @Override
    public JobResponse createJob(JobCreateRequest request, String recruiterEmail) {
        User recruiter = getAuthenticatedRecruiter(recruiterEmail);

        JobStatus initialStatus = Boolean.TRUE.equals(request.getPublishImmediately())
                ? JobStatus.PUBLISHED
                : JobStatus.DRAFT;

        Job job = new Job();
        job.setTitle(request.getTitle().trim());
        job.setDescription(request.getDescription().trim());
        job.setLocation(request.getLocation().trim());
        job.setEmploymentType(request.getEmploymentType());
        job.setSalaryRange(request.getSalaryRange() != null ? request.getSalaryRange().trim() : null);
        job.setRequiredSkills(request.getRequiredSkills());
        job.setStatus(initialStatus);
        job.setRecruiter(recruiter);

        Job savedJob = jobRepository.save(job);
        return JobResponse.fromEntity(savedJob);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<JobResponse> getMyJobs(String recruiterEmail, Pageable pageable) {
        User recruiter = getAuthenticatedRecruiter(recruiterEmail);
        return jobRepository.findByRecruiterId(recruiter.getId(), pageable).map(JobResponse::fromEntity);
    }

    @Override
    public JobResponse updateJob(Long id, JobUpdateRequest request, String recruiterEmail) {
        User recruiter = getAuthenticatedRecruiter(recruiterEmail);
        Job job = getJobAndVerifyOwnership(id, recruiter);

        job.setTitle(request.getTitle().trim());
        job.setDescription(request.getDescription().trim());
        job.setLocation(request.getLocation().trim());
        job.setEmploymentType(request.getEmploymentType());
        job.setSalaryRange(request.getSalaryRange() != null ? request.getSalaryRange().trim() : null);
        job.setRequiredSkills(request.getRequiredSkills());

        Job updatedJob = jobRepository.save(job);
        return JobResponse.fromEntity(updatedJob);
    }

    @Override
    public JobResponse updateJobStatus(Long id, JobStatus status, String recruiterEmail) {
        User recruiter = getAuthenticatedRecruiter(recruiterEmail);
        Job job = getJobAndVerifyOwnership(id, recruiter);

        job.setStatus(status);

        Job updatedJob = jobRepository.save(job);
        return JobResponse.fromEntity(updatedJob);
    }

    @Override
    public void deleteJob(Long id, String recruiterEmail) {
        User recruiter = getAuthenticatedRecruiter(recruiterEmail);
        Job job = getJobAndVerifyOwnership(id, recruiter);

        jobRepository.delete(job);
    }

    private User getAuthenticatedRecruiter(String email) {
        if (email == null || email.isBlank()) {
            throw new AccessDeniedException("Authentication required");
        }
        User user = userRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        if (user.getRole() != Role.RECRUITER) {
            throw new AccessDeniedException("Only recruiters are permitted to perform this operation");
        }
        return user;
    }

    private Job getJobAndVerifyOwnership(Long jobId, User recruiter) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with id: " + jobId));

        if (!job.getRecruiter().getId().equals(recruiter.getId())) {
            throw new AccessDeniedException("You do not have permission to modify this job posting");
        }
        return job;
    }
}
