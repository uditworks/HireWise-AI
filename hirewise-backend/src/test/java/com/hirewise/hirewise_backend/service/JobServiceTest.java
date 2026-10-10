package com.hirewise.hirewise_backend.service;

import com.hirewise.hirewise_backend.dto.JobCreateRequest;
import com.hirewise.hirewise_backend.dto.JobResponse;
import com.hirewise.hirewise_backend.dto.JobUpdateRequest;
import com.hirewise.hirewise_backend.entity.EmploymentType;
import com.hirewise.hirewise_backend.entity.Job;
import com.hirewise.hirewise_backend.entity.JobStatus;
import com.hirewise.hirewise_backend.entity.Role;
import com.hirewise.hirewise_backend.entity.User;
import com.hirewise.hirewise_backend.exception.ResourceNotFoundException;
import com.hirewise.hirewise_backend.repository.JobRepository;
import com.hirewise.hirewise_backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JobServiceTest {

    @Mock
    private JobRepository jobRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private JobServiceImpl jobService;

    private User recruiterJane;
    private User recruiterOther;
    private User jobSeeker;
    private Job publishedJob;
    private Job draftJob;

    @BeforeEach
    void setUp() {
        recruiterJane = new User(2L, "Jane Recruiter", "jane@company.com", "hash", Role.RECRUITER, LocalDateTime.now());
        recruiterOther = new User(5L, "Bob Other", "bob@other.com", "hash", Role.RECRUITER, LocalDateTime.now());
        jobSeeker = new User(1L, "John Seeker", "john@example.com", "hash", Role.JOB_SEEKER, LocalDateTime.now());

        publishedJob = new Job(10L, "Senior Java Engineer", "Build backend APIs", "Remote",
                EmploymentType.FULL_TIME, "$140,000 - $160,000", Set.of("Java", "Spring Boot"),
                JobStatus.PUBLISHED, recruiterJane, LocalDateTime.now(), LocalDateTime.now());

        draftJob = new Job(11L, "Draft Tech Lead", "Draft requirements", "New York, NY",
                EmploymentType.FULL_TIME, "$180,000", Set.of("Architecture", "Java"),
                JobStatus.DRAFT, recruiterJane, LocalDateTime.now(), LocalDateTime.now());
    }

    @Test
    @DisplayName("createJob succeeds when user is a RECRUITER")
    void createJob_Success() {
        JobCreateRequest request = new JobCreateRequest(
                "Backend Engineer", "Build microservices", "Remote",
                EmploymentType.FULL_TIME, "$120k-$140k", Set.of("Java", "MySQL"), false
        );

        when(userRepository.findByEmail("jane@company.com")).thenReturn(Optional.of(recruiterJane));
        when(jobRepository.save(any(Job.class))).thenAnswer(invocation -> {
            Job j = invocation.getArgument(0);
            j.setId(100L);
            return j;
        });

        JobResponse response = jobService.createJob(request, "jane@company.com");

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(100L);
        assertThat(response.getTitle()).isEqualTo("Backend Engineer");
        assertThat(response.getStatus()).isEqualTo(JobStatus.DRAFT);
        assertThat(response.getRecruiter().getEmail()).isEqualTo("jane@company.com");
    }

    @Test
    @DisplayName("createJob with publishImmediately=true creates PUBLISHED job")
    void createJob_PublishImmediately_Success() {
        JobCreateRequest request = new JobCreateRequest(
                "DevOps Engineer", "Manage Kubernetes clusters", "Remote",
                EmploymentType.CONTRACT, "$100/hr", Set.of("Kubernetes", "AWS"), true
        );

        when(userRepository.findByEmail("jane@company.com")).thenReturn(Optional.of(recruiterJane));
        when(jobRepository.save(any(Job.class))).thenAnswer(invocation -> invocation.getArgument(0));

        JobResponse response = jobService.createJob(request, "jane@company.com");

        assertThat(response.getStatus()).isEqualTo(JobStatus.PUBLISHED);
    }

    @Test
    @DisplayName("createJob fails with AccessDeniedException when caller is JOB_SEEKER")
    void createJob_JobSeeker_ThrowsAccessDenied() {
        JobCreateRequest request = new JobCreateRequest(
                "Backend Engineer", "Build microservices", "Remote",
                EmploymentType.FULL_TIME, null, Set.of("Java"), false
        );

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(jobSeeker));

        assertThatThrownBy(() -> jobService.createJob(request, "john@example.com"))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Only recruiters are permitted");
    }

    @Test
    @DisplayName("getJobById returns PUBLISHED job for unauthenticated caller")
    void getJobById_Published_PublicAccess_Success() {
        when(jobRepository.findById(10L)).thenReturn(Optional.of(publishedJob));

        JobResponse response = jobService.getJobById(10L, null);

        assertThat(response).isNotNull();
        assertThat(response.getTitle()).isEqualTo("Senior Java Engineer");
    }

    @Test
    @DisplayName("getJobById returns DRAFT job when requested by owning recruiter")
    void getJobById_Draft_Owner_Success() {
        when(jobRepository.findById(11L)).thenReturn(Optional.of(draftJob));

        JobResponse response = jobService.getJobById(11L, "jane@company.com");

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(JobStatus.DRAFT);
    }

    @Test
    @DisplayName("getJobById throws ResourceNotFoundException for DRAFT job when requested by public or non-owner")
    void getJobById_Draft_NonOwner_ThrowsNotFound() {
        when(jobRepository.findById(11L)).thenReturn(Optional.of(draftJob));

        assertThatThrownBy(() -> jobService.getJobById(11L, "other@example.com"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Job not found with id: 11");

        assertThatThrownBy(() -> jobService.getJobById(11L, null))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Job not found with id: 11");
    }

    @Test
    @DisplayName("updateJob succeeds when caller is the owning recruiter")
    void updateJob_Owner_Success() {
        JobUpdateRequest request = new JobUpdateRequest(
                "Updated Lead", "Updated description content", "Remote",
                EmploymentType.FULL_TIME, "$190,000", Set.of("Java", "Kubernetes")
        );

        when(userRepository.findByEmail("jane@company.com")).thenReturn(Optional.of(recruiterJane));
        when(jobRepository.findById(10L)).thenReturn(Optional.of(publishedJob));
        when(jobRepository.save(any(Job.class))).thenAnswer(invocation -> invocation.getArgument(0));

        JobResponse response = jobService.updateJob(10L, request, "jane@company.com");

        assertThat(response.getTitle()).isEqualTo("Updated Lead");
        assertThat(response.getDescription()).isEqualTo("Updated description content");
    }

    @Test
    @DisplayName("updateJob throws AccessDeniedException when caller is a different recruiter")
    void updateJob_DifferentRecruiter_ThrowsAccessDenied() {
        JobUpdateRequest request = new JobUpdateRequest(
                "Hacked Title", "Hacked description", "Remote",
                EmploymentType.FULL_TIME, "$200,000", Set.of("Java")
        );

        when(userRepository.findByEmail("bob@other.com")).thenReturn(Optional.of(recruiterOther));
        when(jobRepository.findById(10L)).thenReturn(Optional.of(publishedJob));

        assertThatThrownBy(() -> jobService.updateJob(10L, request, "bob@other.com"))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("You do not have permission to modify this job posting");
    }

    @Test
    @DisplayName("updateJobStatus transitions status when caller is the owner")
    void updateJobStatus_Owner_Success() {
        when(userRepository.findByEmail("jane@company.com")).thenReturn(Optional.of(recruiterJane));
        when(jobRepository.findById(10L)).thenReturn(Optional.of(publishedJob));
        when(jobRepository.save(any(Job.class))).thenAnswer(invocation -> invocation.getArgument(0));

        JobResponse response = jobService.updateJobStatus(10L, JobStatus.CLOSED, "jane@company.com");

        assertThat(response.getStatus()).isEqualTo(JobStatus.CLOSED);
    }

    @Test
    @DisplayName("updateJobStatus throws AccessDeniedException when caller is not the owner")
    void updateJobStatus_DifferentRecruiter_ThrowsAccessDenied() {
        when(userRepository.findByEmail("bob@other.com")).thenReturn(Optional.of(recruiterOther));
        when(jobRepository.findById(10L)).thenReturn(Optional.of(publishedJob));

        assertThatThrownBy(() -> jobService.updateJobStatus(10L, JobStatus.CLOSED, "bob@other.com"))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("You do not have permission to modify this job posting");
    }

    @Test
    @DisplayName("deleteJob succeeds when caller is the owner")
    void deleteJob_Owner_Success() {
        when(userRepository.findByEmail("jane@company.com")).thenReturn(Optional.of(recruiterJane));
        when(jobRepository.findById(10L)).thenReturn(Optional.of(publishedJob));

        jobService.deleteJob(10L, "jane@company.com");

        verify(jobRepository).delete(publishedJob);
    }

    @Test
    @DisplayName("deleteJob throws AccessDeniedException when caller is not the owner")
    void deleteJob_DifferentRecruiter_ThrowsAccessDenied() {
        when(userRepository.findByEmail("bob@other.com")).thenReturn(Optional.of(recruiterOther));
        when(jobRepository.findById(10L)).thenReturn(Optional.of(publishedJob));

        assertThatThrownBy(() -> jobService.deleteJob(10L, "bob@other.com"))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("You do not have permission to modify this job posting");
    }

    @Test
    @DisplayName("getMyJobs returns recruiter's postings")
    void getMyJobs_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        when(userRepository.findByEmail("jane@company.com")).thenReturn(Optional.of(recruiterJane));
        when(jobRepository.findByRecruiterId(2L, pageable)).thenReturn(new PageImpl<>(List.of(publishedJob, draftJob)));

        Page<JobResponse> result = jobService.getMyJobs("jane@company.com", pageable);

        assertThat(result.getContent()).hasSize(2);
    }

    @Test
    @DisplayName("getPublishedJobs delegates to repository specification")
    void getPublishedJobs_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        when(jobRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(publishedJob)));

        Page<JobResponse> result = jobService.getPublishedJobs("Java", "Remote", EmploymentType.FULL_TIME, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("Senior Java Engineer");
    }
}
