package com.hirewise.hirewise_backend.service;

import com.hirewise.hirewise_backend.dto.DownloadedResume;
import com.hirewise.hirewise_backend.dto.JobMatchResponse;
import com.hirewise.hirewise_backend.dto.ResumeAnalysisResponse;
import com.hirewise.hirewise_backend.dto.ResumeResponse;
import com.hirewise.hirewise_backend.dto.ResumeSummaryResponse;
import com.hirewise.hirewise_backend.entity.Job;
import com.hirewise.hirewise_backend.entity.JobStatus;
import com.hirewise.hirewise_backend.entity.Resume;
import com.hirewise.hirewise_backend.entity.ResumeProcessingStatus;
import com.hirewise.hirewise_backend.entity.Role;
import com.hirewise.hirewise_backend.entity.User;
import com.hirewise.hirewise_backend.exception.ResourceNotFoundException;
import com.hirewise.hirewise_backend.repository.JobApplicationRepository;
import com.hirewise.hirewise_backend.repository.JobRepository;
import com.hirewise.hirewise_backend.repository.ResumeRepository;
import com.hirewise.hirewise_backend.repository.UserRepository;
import com.hirewise.hirewise_backend.service.ai.AiResumeAnalyzerService;
import com.hirewise.hirewise_backend.service.pdf.PdfTextExtractorService;
import com.hirewise.hirewise_backend.service.storage.StorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResumeServiceTest {

    @Mock
    private ResumeRepository resumeRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JobRepository jobRepository;

    @Mock
    private JobApplicationRepository jobApplicationRepository;

    @Mock
    private StorageService storageService;

    @Mock
    private PdfTextExtractorService pdfTextExtractorService;

    @Mock
    private AiResumeAnalyzerService aiResumeAnalyzerService;

    @InjectMocks
    private ResumeServiceImpl resumeService;

    private User seekerAlice;
    private User seekerBob;
    private User recruiterJane;
    private User recruiterOther;
    private Job publishedJob;
    private Resume aliceResume;

    @BeforeEach
    void setUp() {
        seekerAlice = new User("Alice Seeker", "alice@example.com", "hash", Role.JOB_SEEKER);
        seekerAlice.setId(1L);

        seekerBob = new User("Bob Seeker", "bob@example.com", "hash", Role.JOB_SEEKER);
        seekerBob.setId(2L);

        recruiterJane = new User("Jane Recruiter", "jane@company.com", "hash", Role.RECRUITER);
        recruiterJane.setId(10L);

        recruiterOther = new User("Other Recruiter", "other@corp.com", "hash", Role.RECRUITER);
        recruiterOther.setId(20L);

        publishedJob = new Job();
        publishedJob.setId(50L);
        publishedJob.setTitle("Full Stack Developer");
        publishedJob.setDescription("Full stack engineering role");
        publishedJob.setLocation("San Francisco");
        publishedJob.setEmploymentType(com.hirewise.hirewise_backend.entity.EmploymentType.FULL_TIME);
        publishedJob.setSalaryRange("$100k-$130k");
        publishedJob.setRequiredSkills(Set.of("Java", "React"));
        publishedJob.setRecruiter(recruiterJane);
        publishedJob.setStatus(JobStatus.PUBLISHED);

        aliceResume = new Resume(seekerAlice, "alice_resume.pdf", "alice-uuid.pdf", 1024L, "application/pdf");
        aliceResume.setId(100L);
        aliceResume.setExtractedText("Alice Seeker - Java and React developer");
        aliceResume.setProcessingStatus(ResumeProcessingStatus.PARSED);
    }

    @Test
    @DisplayName("Should successfully upload resume by job seeker")
    void testUploadResumeSuccess() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "alice_resume.pdf", "application/pdf", "%PDF-1.4 test data".getBytes()
        );

        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(seekerAlice));
        when(pdfTextExtractorService.extractText(any())).thenReturn("Extracted Java text");
        when(storageService.store(any(), anyString())).thenReturn("new-uuid.pdf");
        when(resumeRepository.save(any(Resume.class))).thenAnswer(invocation -> {
            Resume r = invocation.getArgument(0);
            r.setId(100L);
            return r;
        });

        ResumeResponse response = resumeService.uploadResume(file, "alice@example.com");

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(100L);
        assertThat(response.getOriginalFilename()).isEqualTo("alice_resume.pdf");
        assertThat(response.getProcessingStatus()).isEqualTo(ResumeProcessingStatus.PARSED);

        verify(storageService).store(any(), anyString());
        verify(resumeRepository).save(any(Resume.class));
    }

    @Test
    @DisplayName("Should reject upload when user is not a job seeker")
    void testUploadResumeNonSeekerRejected() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "resume.pdf", "application/pdf", "%PDF-1.4 test".getBytes()
        );
        when(userRepository.findByEmail("jane@company.com")).thenReturn(Optional.of(recruiterJane));

        assertThatThrownBy(() -> resumeService.uploadResume(file, "jane@company.com"))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Only job seekers");
    }

    @Test
    @DisplayName("Should reject upload for empty file")
    void testUploadEmptyFileRejected() {
        MockMultipartFile file = new MockMultipartFile("file", "empty.pdf", "application/pdf", new byte[0]);
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(seekerAlice));

        assertThatThrownBy(() -> resumeService.uploadResume(file, "alice@example.com"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cannot be empty");
    }

    @Test
    @DisplayName("Should replace resume safely, removing old storage file only after new one is persisted")
    void testReplaceResumeSuccess() {
        MockMultipartFile newFile = new MockMultipartFile(
                "file", "updated_resume.pdf", "application/pdf", "%PDF-1.4 updated".getBytes()
        );

        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(seekerAlice));
        when(resumeRepository.findByIdAndOwnerId(100L, 1L)).thenReturn(Optional.of(aliceResume));
        when(pdfTextExtractorService.extractText(any())).thenReturn("Updated Java text");
        when(storageService.store(any(), anyString())).thenReturn("new-replaced-uuid.pdf");
        when(resumeRepository.save(any(Resume.class))).thenReturn(aliceResume);

        ResumeResponse response = resumeService.replaceResume(100L, newFile, "alice@example.com");

        assertThat(response).isNotNull();
        verify(storageService).store(any(), eq("updated_resume.pdf"));
        verify(storageService).delete("alice-uuid.pdf"); // Old file deleted only after save
    }

    @Test
    @DisplayName("Should return list of resumes owned by seeker")
    void testGetMyResumes() {
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(seekerAlice));
        when(resumeRepository.findByOwnerIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(aliceResume));

        List<ResumeSummaryResponse> result = resumeService.getMyResumes("alice@example.com");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(100L);
    }

    @Test
    @DisplayName("Should allow recruiter to view candidate resume when candidate has applied to recruiter's job")
    void testRecruiterViewResumeAllowedWhenApplied() {
        when(userRepository.findByEmail("jane@company.com")).thenReturn(Optional.of(recruiterJane));
        when(resumeRepository.findById(100L)).thenReturn(Optional.of(aliceResume));
        when(jobApplicationRepository.existsByResumeIdAndJobRecruiterId(100L, 10L)).thenReturn(true);

        ResumeResponse response = resumeService.getResumeById(100L, "jane@company.com");

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(100L);
    }

    @Test
    @DisplayName("Should deny recruiter viewing candidate resume when candidate has NOT applied")
    void testRecruiterViewResumeDeniedWhenNotApplied() {
        when(userRepository.findByEmail("other@corp.com")).thenReturn(Optional.of(recruiterOther));
        when(resumeRepository.findById(100L)).thenReturn(Optional.of(aliceResume));
        when(jobApplicationRepository.existsByResumeIdAndJobRecruiterId(100L, 20L)).thenReturn(false);
        when(jobApplicationRepository.existsByApplicantIdAndJobRecruiterId(1L, 20L)).thenReturn(false);

        assertThatThrownBy(() -> resumeService.getResumeById(100L, "other@corp.com"))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("You do not have permission");
    }

    @Test
    @DisplayName("Should download resume for owner seeker")
    void testDownloadResumeOwner() {
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(seekerAlice));
        when(resumeRepository.findById(100L)).thenReturn(Optional.of(aliceResume));
        when(storageService.load("alice-uuid.pdf")).thenReturn("%PDF-1.4 raw data".getBytes());

        DownloadedResume downloaded = resumeService.downloadResume(100L, "alice@example.com");

        assertThat(downloaded).isNotNull();
        assertThat(downloaded.getFilename()).isEqualTo("alice_resume.pdf");
        assertThat(downloaded.getContent()).isNotEmpty();
    }

    @Test
    @DisplayName("Should safely delete resume: nullify applications references, delete file, delete record")
    void testDeleteResumeSuccess() {
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(seekerAlice));
        when(resumeRepository.findByIdAndOwnerId(100L, 1L)).thenReturn(Optional.of(aliceResume));

        resumeService.deleteResume(100L, "alice@example.com");

        verify(jobApplicationRepository).nullifyResumeReferences(100L);
        verify(storageService).delete("alice-uuid.pdf");
        verify(resumeRepository).delete(aliceResume);
    }

    @Test
    @DisplayName("Should perform resume analysis for owner seeker")
    void testAnalyzeResume() {
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(seekerAlice));
        when(resumeRepository.findByIdAndOwnerId(100L, 1L)).thenReturn(Optional.of(aliceResume));

        ResumeAnalysisResponse mockAnalysis = new ResumeAnalysisResponse(
                100L, "Profile summary", Set.of("Java", "React"), List.of("Strong skills"), List.of(), List.of()
        );
        when(aiResumeAnalyzerService.analyzeResume(aliceResume)).thenReturn(mockAnalysis);

        ResumeAnalysisResponse response = resumeService.analyzeResume(100L, "alice@example.com");

        assertThat(response).isNotNull();
        assertThat(response.getResumeId()).isEqualTo(100L);
        assertThat(response.getDetectedSkills()).contains("Java", "React");
    }

    @Test
    @DisplayName("Should match resume to job for owner seeker")
    void testMatchResumeToJob() {
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(seekerAlice));
        when(resumeRepository.findById(100L)).thenReturn(Optional.of(aliceResume));
        when(jobRepository.findById(50L)).thenReturn(Optional.of(publishedJob));

        JobMatchResponse mockMatch = new JobMatchResponse(
                100L, 50L, "Full Stack Developer", 85, 90, 80, "High Match",
                List.of("Java", "React"), List.of(), "Great fit"
        );
        when(aiResumeAnalyzerService.matchResumeToJob(aliceResume, publishedJob)).thenReturn(mockMatch);

        JobMatchResponse response = resumeService.matchResumeToJob(100L, 50L, "alice@example.com");

        assertThat(response).isNotNull();
        assertThat(response.getOverallMatchScore()).isEqualTo(85);
        assertThat(response.getFitCategory()).isEqualTo("High Match");
    }
}
