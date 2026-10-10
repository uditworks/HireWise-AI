package com.hirewise.hirewise_backend.service;

import com.hirewise.hirewise_backend.dto.JobApplicationCreateRequest;
import com.hirewise.hirewise_backend.dto.JobApplicationResponse;
import com.hirewise.hirewise_backend.entity.ApplicationStatus;
import com.hirewise.hirewise_backend.entity.EmploymentType;
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
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.hirewise.hirewise_backend.repository.ResumeRepository;

@ExtendWith(MockitoExtension.class)
class JobApplicationServiceTest {

    @Mock
    private JobApplicationRepository jobApplicationRepository;

    @Mock
    private JobRepository jobRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ResumeRepository resumeRepository;

    @InjectMocks
    private JobApplicationServiceImpl jobApplicationService;

    private User seekerAlice;
    private User seekerBob;
    private User recruiterJane;
    private User recruiterOther;
    private Job publishedJob;
    private Job draftJob;
    private JobApplication activeApplication;

    @BeforeEach
    void setUp() {
        seekerAlice = new User(1L, "Alice Seeker", "alice@example.com", "hash", Role.JOB_SEEKER, LocalDateTime.now());
        seekerBob = new User(2L, "Bob Seeker", "bob@example.com", "hash", Role.JOB_SEEKER, LocalDateTime.now());
        recruiterJane = new User(3L, "Jane Recruiter", "jane@company.com", "hash", Role.RECRUITER, LocalDateTime.now());
        recruiterOther = new User(4L, "Other Recruiter", "other@company.com", "hash", Role.RECRUITER, LocalDateTime.now());

        publishedJob = new Job(10L, "Java Architect", "Build cloud backend", "Remote",
                EmploymentType.FULL_TIME, "$160,000", Set.of("Java"), JobStatus.PUBLISHED,
                recruiterJane, LocalDateTime.now(), LocalDateTime.now());

        draftJob = new Job(11L, "Draft Position", "Internal only", "Remote",
                EmploymentType.FULL_TIME, "$100,000", Set.of("Java"), JobStatus.DRAFT,
                recruiterJane, LocalDateTime.now(), LocalDateTime.now());

        activeApplication = new JobApplication(100L, publishedJob, seekerAlice, ApplicationStatus.SUBMITTED,
                "Strong candidate cover letter", LocalDateTime.now(), LocalDateTime.now());
    }

    @Test
    @DisplayName("applyToJob succeeds when seeker applies to published job")
    void applyToJob_Success() {
        JobApplicationCreateRequest request = new JobApplicationCreateRequest("Excited to apply!");

        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(seekerAlice));
        when(jobRepository.findById(10L)).thenReturn(Optional.of(publishedJob));
        when(jobApplicationRepository.existsByJobIdAndApplicantId(10L, 1L)).thenReturn(false);
        when(jobApplicationRepository.save(any(JobApplication.class))).thenAnswer(inv -> {
            JobApplication app = inv.getArgument(0);
            app.setId(101L);
            return app;
        });

        JobApplicationResponse response = jobApplicationService.applyToJob(10L, request, "alice@example.com");

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(101L);
        assertThat(response.getStatus()).isEqualTo(ApplicationStatus.SUBMITTED);
        assertThat(response.getCoverLetter()).isEqualTo("Excited to apply!");
        assertThat(response.getApplicant().getEmail()).isEqualTo("alice@example.com");
        assertThat(response.getJob().getId()).isEqualTo(10L);
    }

    @Test
    @DisplayName("applyToJob throws IllegalArgumentException when job is not PUBLISHED")
    void applyToJob_UnpublishedJob_ThrowsIllegalArgument() {
        JobApplicationCreateRequest request = new JobApplicationCreateRequest("Excited to apply!");

        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(seekerAlice));
        when(jobRepository.findById(11L)).thenReturn(Optional.of(draftJob));

        assertThatThrownBy(() -> jobApplicationService.applyToJob(11L, request, "alice@example.com"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Applications are only accepted for published jobs");
    }

    @Test
    @DisplayName("applyToJob throws AccessDeniedException when caller is a RECRUITER")
    void applyToJob_RecruiterRole_ThrowsAccessDenied() {
        when(userRepository.findByEmail("jane@company.com")).thenReturn(Optional.of(recruiterJane));

        assertThatThrownBy(() -> jobApplicationService.applyToJob(10L, new JobApplicationCreateRequest(), "jane@company.com"))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Only job seekers can apply for jobs");
    }

    @Test
    @DisplayName("applyToJob throws DuplicateResourceException when applicant has already applied")
    void applyToJob_Duplicate_ThrowsDuplicateResource() {
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(seekerAlice));
        when(jobRepository.findById(10L)).thenReturn(Optional.of(publishedJob));
        when(jobApplicationRepository.existsByJobIdAndApplicantId(10L, 1L)).thenReturn(true);

        assertThatThrownBy(() -> jobApplicationService.applyToJob(10L, new JobApplicationCreateRequest(), "alice@example.com"))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("You have already applied for this job");
    }

    @Test
    @DisplayName("getMyApplications returns paginated applications for job seeker")
    void getMyApplications_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(seekerAlice));
        when(jobApplicationRepository.findByApplicantId(1L, pageable))
                .thenReturn(new PageImpl<>(List.of(activeApplication)));

        Page<JobApplicationResponse> result = jobApplicationService.getMyApplications("alice@example.com", pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getId()).isEqualTo(100L);
    }

    @Test
    @DisplayName("getApplicationById allows applicant to view their own application")
    void getApplicationById_Applicant_Success() {
        when(jobApplicationRepository.findById(100L)).thenReturn(Optional.of(activeApplication));
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(seekerAlice));

        JobApplicationResponse response = jobApplicationService.getApplicationById(100L, "alice@example.com");

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(100L);
    }

    @Test
    @DisplayName("getApplicationById allows owning recruiter to view application for their job")
    void getApplicationById_OwningRecruiter_Success() {
        when(jobApplicationRepository.findById(100L)).thenReturn(Optional.of(activeApplication));
        when(userRepository.findByEmail("jane@company.com")).thenReturn(Optional.of(recruiterJane));

        JobApplicationResponse response = jobApplicationService.getApplicationById(100L, "jane@company.com");

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(100L);
    }

    @Test
    @DisplayName("getApplicationById throws AccessDeniedException for unrelated user")
    void getApplicationById_UnrelatedUser_ThrowsAccessDenied() {
        when(jobApplicationRepository.findById(100L)).thenReturn(Optional.of(activeApplication));
        when(userRepository.findByEmail("bob@example.com")).thenReturn(Optional.of(seekerBob));

        assertThatThrownBy(() -> jobApplicationService.getApplicationById(100L, "bob@example.com"))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("You do not have permission to view this application");
    }

    @Test
    @DisplayName("withdrawApplication transitions SUBMITTED application to WITHDRAWN")
    void withdrawApplication_Success() {
        when(jobApplicationRepository.findById(100L)).thenReturn(Optional.of(activeApplication));
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(seekerAlice));
        when(jobApplicationRepository.save(any(JobApplication.class))).thenAnswer(inv -> inv.getArgument(0));

        JobApplicationResponse response = jobApplicationService.withdrawApplication(100L, "alice@example.com");

        assertThat(response.getStatus()).isEqualTo(ApplicationStatus.WITHDRAWN);
    }

    @Test
    @DisplayName("withdrawApplication throws IllegalArgumentException if already withdrawn")
    void withdrawApplication_AlreadyWithdrawn_ThrowsIllegalArgument() {
        activeApplication.setStatus(ApplicationStatus.WITHDRAWN);
        when(jobApplicationRepository.findById(100L)).thenReturn(Optional.of(activeApplication));
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(seekerAlice));

        assertThatThrownBy(() -> jobApplicationService.withdrawApplication(100L, "alice@example.com"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Application is already withdrawn");
    }

    @Test
    @DisplayName("withdrawApplication throws AccessDeniedException if caller is not the applicant")
    void withdrawApplication_NotOwner_ThrowsAccessDenied() {
        when(jobApplicationRepository.findById(100L)).thenReturn(Optional.of(activeApplication));
        when(userRepository.findByEmail("bob@example.com")).thenReturn(Optional.of(seekerBob));

        assertThatThrownBy(() -> jobApplicationService.withdrawApplication(100L, "bob@example.com"))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("You do not have permission to withdraw this application");
    }

    @Test
    @DisplayName("getApplicationsForJob allows owning recruiter to list applicants")
    void getApplicationsForJob_Owner_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        when(jobRepository.findById(10L)).thenReturn(Optional.of(publishedJob));
        when(userRepository.findByEmail("jane@company.com")).thenReturn(Optional.of(recruiterJane));
        when(jobApplicationRepository.findByJobId(10L, pageable))
                .thenReturn(new PageImpl<>(List.of(activeApplication)));

        Page<JobApplicationResponse> result = jobApplicationService.getApplicationsForJob(10L, null, "jane@company.com", pageable);

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("getApplicationsForJob throws AccessDeniedException if caller is a different recruiter")
    void getApplicationsForJob_NotOwner_ThrowsAccessDenied() {
        when(jobRepository.findById(10L)).thenReturn(Optional.of(publishedJob));
        when(userRepository.findByEmail("other@company.com")).thenReturn(Optional.of(recruiterOther));

        assertThatThrownBy(() -> jobApplicationService.getApplicationsForJob(10L, null, "other@company.com", PageRequest.of(0, 10)))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("You do not have permission to view applications for this job");
    }

    @Test
    @DisplayName("updateApplicationStatus transitions status when owning recruiter updates it")
    void updateApplicationStatus_Success() {
        when(jobApplicationRepository.findById(100L)).thenReturn(Optional.of(activeApplication));
        when(userRepository.findByEmail("jane@company.com")).thenReturn(Optional.of(recruiterJane));
        when(jobApplicationRepository.save(any(JobApplication.class))).thenAnswer(inv -> inv.getArgument(0));

        JobApplicationResponse response = jobApplicationService.updateApplicationStatus(100L, ApplicationStatus.SHORTLISTED, "jane@company.com");

        assertThat(response.getStatus()).isEqualTo(ApplicationStatus.SHORTLISTED);
    }

    @Test
    @DisplayName("updateApplicationStatus throws IllegalArgumentException if application is WITHDRAWN")
    void updateApplicationStatus_Withdrawn_ThrowsIllegalArgument() {
        activeApplication.setStatus(ApplicationStatus.WITHDRAWN);
        when(jobApplicationRepository.findById(100L)).thenReturn(Optional.of(activeApplication));
        when(userRepository.findByEmail("jane@company.com")).thenReturn(Optional.of(recruiterJane));

        assertThatThrownBy(() -> jobApplicationService.updateApplicationStatus(100L, ApplicationStatus.INTERVIEW, "jane@company.com"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cannot update status of a withdrawn application");
    }

    @Test
    @DisplayName("updateApplicationStatus throws IllegalArgumentException if recruiter attempts to set WITHDRAWN")
    void updateApplicationStatus_RecruiterSetsWithdrawn_ThrowsIllegalArgument() {
        when(jobApplicationRepository.findById(100L)).thenReturn(Optional.of(activeApplication));
        when(userRepository.findByEmail("jane@company.com")).thenReturn(Optional.of(recruiterJane));

        assertThatThrownBy(() -> jobApplicationService.updateApplicationStatus(100L, ApplicationStatus.WITHDRAWN, "jane@company.com"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Recruiters cannot withdraw applications");
    }

    @Test
    @DisplayName("updateApplicationStatus throws AccessDeniedException if caller is not the owning recruiter")
    void updateApplicationStatus_NotOwner_ThrowsAccessDenied() {
        when(jobApplicationRepository.findById(100L)).thenReturn(Optional.of(activeApplication));
        when(userRepository.findByEmail("other@company.com")).thenReturn(Optional.of(recruiterOther));

        assertThatThrownBy(() -> jobApplicationService.updateApplicationStatus(100L, ApplicationStatus.INTERVIEW, "other@company.com"))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("You do not have permission to update applications for this job");
    }
}
