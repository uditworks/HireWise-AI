package com.hirewise.hirewise_backend.service;

import com.hirewise.hirewise_backend.dto.JobApplicationCreateRequest;
import com.hirewise.hirewise_backend.dto.JobApplicationResponse;
import com.hirewise.hirewise_backend.entity.ApplicationStatus;
import com.hirewise.hirewise_backend.entity.Job;
import com.hirewise.hirewise_backend.entity.JobApplication;
import com.hirewise.hirewise_backend.entity.JobStatus;
import com.hirewise.hirewise_backend.entity.Role;
import com.hirewise.hirewise_backend.entity.User;
import com.hirewise.hirewise_backend.exception.DuplicateResourceException;
import com.hirewise.hirewise_backend.exception.ResourceNotFoundException;
import com.hirewise.hirewise_backend.repository.JobApplicationRepository;
import com.hirewise.hirewise_backend.repository.JobRepository;
import com.hirewise.hirewise_backend.repository.UserRepository;
import com.hirewise.hirewise_backend.entity.Resume;
import com.hirewise.hirewise_backend.repository.ResumeRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class JobApplicationServiceImpl implements JobApplicationService {

    private final JobApplicationRepository jobApplicationRepository;
    private final JobRepository jobRepository;
    private final UserRepository userRepository;
    private final ResumeRepository resumeRepository;

    public JobApplicationServiceImpl(JobApplicationRepository jobApplicationRepository,
                                     JobRepository jobRepository,
                                     UserRepository userRepository,
                                     ResumeRepository resumeRepository) {
        this.jobApplicationRepository = jobApplicationRepository;
        this.jobRepository = jobRepository;
        this.userRepository = userRepository;
        this.resumeRepository = resumeRepository;
    }

    @Override
    public JobApplicationResponse applyToJob(Long jobId, JobApplicationCreateRequest request, String applicantEmail) {
        User applicant = getUser(applicantEmail);
        if (applicant.getRole() != Role.JOB_SEEKER) {
            throw new AccessDeniedException("Only job seekers can apply for jobs");
        }

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with id: " + jobId));

        if (job.getStatus() != JobStatus.PUBLISHED) {
            throw new IllegalArgumentException("Applications are only accepted for published jobs");
        }

        if (jobApplicationRepository.existsByJobIdAndApplicantId(jobId, applicant.getId())) {
            throw new DuplicateResourceException("You have already applied for this job");
        }

        String coverLetter = (request != null && request.getCoverLetter() != null)
                ? request.getCoverLetter().trim()
                : null;

        JobApplication application = new JobApplication(job, applicant, coverLetter);

        if (request != null && request.getResumeId() != null) {
            Resume resume = resumeRepository.findByIdAndOwnerId(request.getResumeId(), applicant.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Resume not found with id: " + request.getResumeId()));
            application.setResume(resume);
        }

        try {
            JobApplication saved = jobApplicationRepository.save(application);
            return JobApplicationResponse.fromEntity(saved);
        } catch (DataIntegrityViolationException ex) {
            throw new DuplicateResourceException("You have already applied for this job");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<JobApplicationResponse> getMyApplications(String applicantEmail, Pageable pageable) {
        User applicant = getUser(applicantEmail);
        if (applicant.getRole() != Role.JOB_SEEKER) {
            throw new AccessDeniedException("Only job seekers can view candidate applications");
        }
        return jobApplicationRepository.findByApplicantId(applicant.getId(), pageable)
                .map(JobApplicationResponse::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public JobApplicationResponse getApplicationById(Long id, String currentUserEmail) {
        JobApplication application = jobApplicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with id: " + id));

        User currentUser = getUser(currentUserEmail);

        boolean isApplicant = application.getApplicant().getId().equals(currentUser.getId());
        boolean isOwningRecruiter = application.getJob().getRecruiter().getId().equals(currentUser.getId());

        if (!isApplicant && !isOwningRecruiter) {
            throw new AccessDeniedException("You do not have permission to view this application");
        }

        return JobApplicationResponse.fromEntity(application);
    }

    @Override
    public JobApplicationResponse withdrawApplication(Long id, String applicantEmail) {
        JobApplication application = jobApplicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with id: " + id));

        User applicant = getUser(applicantEmail);

        if (!application.getApplicant().getId().equals(applicant.getId())) {
            throw new AccessDeniedException("You do not have permission to withdraw this application");
        }

        ApplicationStatus currentStatus = application.getStatus();
        if (currentStatus == ApplicationStatus.WITHDRAWN) {
            throw new IllegalArgumentException("Application is already withdrawn");
        }
        if (currentStatus == ApplicationStatus.REJECTED) {
            throw new IllegalArgumentException("Cannot withdraw an application that has already been rejected");
        }
        if (currentStatus == ApplicationStatus.ACCEPTED) {
            throw new IllegalArgumentException("Cannot withdraw an application that has already been accepted");
        }

        application.setStatus(ApplicationStatus.WITHDRAWN);
        JobApplication updated = jobApplicationRepository.save(application);
        return JobApplicationResponse.fromEntity(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<JobApplicationResponse> getApplicationsForJob(Long jobId, ApplicationStatus status,
                                                              String recruiterEmail, Pageable pageable) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with id: " + jobId));

        User recruiter = getUser(recruiterEmail);
        if (recruiter.getRole() != Role.RECRUITER) {
            throw new AccessDeniedException("Only recruiters can view job applications");
        }

        if (!job.getRecruiter().getId().equals(recruiter.getId())) {
            throw new AccessDeniedException("You do not have permission to view applications for this job");
        }

        Page<JobApplication> page = (status != null)
                ? jobApplicationRepository.findByJobIdAndStatus(jobId, status, pageable)
                : jobApplicationRepository.findByJobId(jobId, pageable);

        return page.map(JobApplicationResponse::fromEntity);
    }

    @Override
    public JobApplicationResponse updateApplicationStatus(Long id, ApplicationStatus newStatus, String recruiterEmail) {
        JobApplication application = jobApplicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with id: " + id));

        User recruiter = getUser(recruiterEmail);
        if (recruiter.getRole() != Role.RECRUITER) {
            throw new AccessDeniedException("Only recruiters can update application status");
        }

        if (!application.getJob().getRecruiter().getId().equals(recruiter.getId())) {
            throw new AccessDeniedException("You do not have permission to update applications for this job");
        }

        if (application.getStatus() == ApplicationStatus.WITHDRAWN) {
            throw new IllegalArgumentException("Cannot update status of a withdrawn application");
        }

        if (newStatus == ApplicationStatus.WITHDRAWN) {
            throw new IllegalArgumentException("Recruiters cannot withdraw applications; withdrawal is candidate-controlled");
        }

        if (application.getStatus() == newStatus) {
            throw new IllegalArgumentException("Application already has status: " + newStatus);
        }

        application.setStatus(newStatus);
        JobApplication updated = jobApplicationRepository.save(application);
        return JobApplicationResponse.fromEntity(updated);
    }

    private User getUser(String email) {
        if (email == null || email.isBlank()) {
            throw new AccessDeniedException("Authentication required");
        }
        return userRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }
}
