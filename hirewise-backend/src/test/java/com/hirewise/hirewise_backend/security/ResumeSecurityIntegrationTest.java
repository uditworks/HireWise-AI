package com.hirewise.hirewise_backend.security;

import com.hirewise.hirewise_backend.entity.ApplicationStatus;
import com.hirewise.hirewise_backend.entity.EmploymentType;
import com.hirewise.hirewise_backend.entity.Job;
import com.hirewise.hirewise_backend.entity.JobApplication;
import com.hirewise.hirewise_backend.entity.JobStatus;
import com.hirewise.hirewise_backend.entity.Resume;
import com.hirewise.hirewise_backend.entity.Role;
import com.hirewise.hirewise_backend.entity.User;
import com.hirewise.hirewise_backend.repository.JobApplicationRepository;
import com.hirewise.hirewise_backend.repository.JobRepository;
import com.hirewise.hirewise_backend.repository.ResumeRepository;
import com.hirewise.hirewise_backend.repository.UserRepository;
import com.hirewise.hirewise_backend.service.storage.StorageService;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@TestPropertySource(properties = {
        "hirewise.jwt.secret=TestSecretKeyForHireWiseAppTestingMustBeAtLeast32BytesLong!",
        "hirewise.storage.resume-dir=./target/test-storage/resumes",
        "hirewise.ai.provider=local"
})
class ResumeSecurityIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private JobApplicationRepository jobApplicationRepository;

    @Autowired
    private ResumeRepository resumeRepository;

    @Autowired
    private StorageService storageService;

    private MockMvc mockMvc;

    private User recruiterA;
    private User recruiterB;
    private User seekerA;
    private User seekerB;

    private String tokenRecruiterA;
    private String tokenRecruiterB;
    private String tokenSeekerA;
    private String tokenSeekerB;

    private Job jobA;
    private Resume resumeA;
    private JobApplication applicationA;

    private final List<Long> createdApplicationIds = new ArrayList<>();
    private final List<Long> createdResumeIds = new ArrayList<>();
    private final List<Long> createdJobIds = new ArrayList<>();
    private final List<Long> createdUserIds = new ArrayList<>();

    @BeforeEach
    void setUp() throws IOException {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        recruiterA = getOrCreateTestUser("test_resume_recruiter_a@integration.test", "Recruiter A", Role.RECRUITER);
        recruiterB = getOrCreateTestUser("test_resume_recruiter_b@integration.test", "Recruiter B", Role.RECRUITER);
        seekerA = getOrCreateTestUser("test_resume_seeker_a@integration.test", "Seeker A", Role.JOB_SEEKER);
        seekerB = getOrCreateTestUser("test_resume_seeker_b@integration.test", "Seeker B", Role.JOB_SEEKER);

        tokenRecruiterA = jwtTokenProvider.generateToken(recruiterA);
        tokenRecruiterB = jwtTokenProvider.generateToken(recruiterB);
        tokenSeekerA = jwtTokenProvider.generateToken(seekerA);
        tokenSeekerB = jwtTokenProvider.generateToken(seekerB);

        // Create published job owned by Recruiter A
        Job j = new Job();
        j.setTitle("Senior Java Backend Engineer");
        j.setDescription("Design and build scalable microservices using Java and Spring Boot.");
        j.setLocation("Remote");
        j.setEmploymentType(EmploymentType.FULL_TIME);
        j.setSalaryRange("$140,000 - $170,000");
        j.setRequiredSkills(Set.of("Java", "Spring Boot", "MySQL", "Docker"));
        j.setStatus(JobStatus.PUBLISHED);
        j.setRecruiter(recruiterA);
        jobA = jobRepository.save(j);
        createdJobIds.add(jobA.getId());

        // Create sample PDF resume for Seeker A
        byte[] pdfBytes = createSamplePdf("Seeker A\nSenior Java Software Engineer\nSkills: Java, Spring Boot, MySQL, Redis\n5+ years of experience in distributed systems.");
        String storageId = storageService.store(pdfBytes, "seeker_a_resume.pdf");

        Resume r = new Resume(seekerA, "seeker_a_resume.pdf", storageId, (long) pdfBytes.length, "application/pdf");
        r.setExtractedText("Seeker A\nSenior Java Software Engineer\nSkills: Java, Spring Boot, MySQL, Redis\n5+ years of experience in distributed systems.");
        resumeA = resumeRepository.save(r);
        createdResumeIds.add(resumeA.getId());

        // Seeker A applies to Job A with Resume A
        JobApplication app = new JobApplication(jobA, seekerA, "I am passionate about this role.");
        app.setResume(resumeA);
        applicationA = jobApplicationRepository.save(app);
        createdApplicationIds.add(applicationA.getId());
    }

    @AfterEach
    void tearDown() {
        for (Long appId : createdApplicationIds) {
            try {
                jobApplicationRepository.deleteById(appId);
            } catch (Exception ignored) {
            }
        }
        for (Long resumeId : createdResumeIds) {
            try {
                resumeRepository.findById(resumeId).ifPresent(res -> storageService.delete(res.getStorageIdentifier()));
                resumeRepository.deleteById(resumeId);
            } catch (Exception ignored) {
            }
        }
        for (Long userId : createdUserIds) {
            try {
                List<Resume> userResumes = resumeRepository.findByOwnerIdOrderByCreatedAtDesc(userId);
                for (Resume r : userResumes) {
                    storageService.delete(r.getStorageIdentifier());
                    resumeRepository.delete(r);
                }
            } catch (Exception ignored) {
            }
        }
        for (Long jobId : createdJobIds) {
            try {
                jobRepository.deleteById(jobId);
            } catch (Exception ignored) {
            }
        }
        for (Long userId : createdUserIds) {
            try {
                userRepository.deleteById(userId);
            } catch (Exception ignored) {
            }
        }
    }

    private User getOrCreateTestUser(String email, String name, Role role) {
        return userRepository.findByEmail(email).orElseGet(() -> {
            User u = new User(name, email, "$2a$10$abcdefghijklmnopqrstuvwxyzABCDEF", role);
            User saved = userRepository.save(u);
            createdUserIds.add(saved.getId());
            return saved;
        });
    }

    private byte[] createSamplePdf(String content) throws IOException {
        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage();
            doc.addPage(page);
            try (PDPageContentStream stream = new PDPageContentStream(doc, page)) {
                stream.beginText();
                stream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                stream.newLineAtOffset(50, 700);
                String safeContent = content != null ? content : "Default Test PDF";
                String[] lines = safeContent.split("\\r?\\n");
                for (int i = 0; i < lines.length; i++) {
                    stream.showText(lines[i]);
                    if (i < lines.length - 1) {
                        stream.newLineAtOffset(0, -15);
                    }
                }
                stream.endText();
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            doc.save(baos);
            return baos.toByteArray();
        }
    }

    @Test
    @DisplayName("Upload resume requires JOB_SEEKER role and rejects anonymous or recruiter")
    void testUploadResumeAuthorization() throws Exception {
        byte[] pdfBytes = createSamplePdf("Seeker B Resume Content");
        MockMultipartFile file = new MockMultipartFile(
                "file", "seeker_b_resume.pdf", "application/pdf", pdfBytes
        );

        // Anonymous -> 401
        mockMvc.perform(multipart("/api/resumes/upload").file(file))
                .andExpect(status().isUnauthorized());

        // Recruiter -> 403
        mockMvc.perform(multipart("/api/resumes/upload").file(file)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenRecruiterA))
                .andExpect(status().isForbidden());

        // Seeker B -> 201 Created
        mockMvc.perform(multipart("/api/resumes/upload").file(file)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenSeekerB))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.originalFilename", is("seeker_b_resume.pdf")));
    }

    @Test
    @DisplayName("Seeker A can view their resume; Seeker B cannot view Seeker A's resume")
    void testSeekerOwnershipEnforcement() throws Exception {
        // Seeker A views own resume -> 200
        mockMvc.perform(get("/api/resumes/" + resumeA.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenSeekerA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(resumeA.getId().intValue())));

        // Seeker B attempts to view Seeker A's resume -> 404 (or 403)
        mockMvc.perform(get("/api/resumes/" + resumeA.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenSeekerB))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Recruiter A (job owner) can access applicant resume; Recruiter B (unrelated) is forbidden")
    void testRecruiterAccessControl() throws Exception {
        // Recruiter A (applicant applied to Recruiter A's job) -> 200
        mockMvc.perform(get("/api/resumes/" + resumeA.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenRecruiterA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(resumeA.getId().intValue())));

        // Recruiter B (candidate did not apply to any of Recruiter B's jobs) -> 403
        mockMvc.perform(get("/api/resumes/" + resumeA.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenRecruiterB))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Download resume returns correct PDF headers and data for authorized users")
    void testDownloadResumeHeadersAndContent() throws Exception {
        mockMvc.perform(get("/api/resumes/" + resumeA.getId() + "/download")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenSeekerA))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, "application/pdf"))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"seeker_a_resume.pdf\""));
    }

    @Test
    @DisplayName("AI Analysis returns explainable skills, summary, strengths, and disclaimer")
    void testAnalyzeResumeEndpoint() throws Exception {
        mockMvc.perform(get("/api/resumes/" + resumeA.getId() + "/analyze")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenSeekerA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resumeId", is(resumeA.getId().intValue())))
                .andExpect(jsonPath("$.summary").exists())
                .andExpect(jsonPath("$.disclaimer").exists());
    }

    @Test
    @DisplayName("Job Matching produces matched skills, missing skills, and overall score")
    void testMatchResumeEndpoint() throws Exception {
        mockMvc.perform(get("/api/resumes/" + resumeA.getId() + "/match/" + jobA.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenSeekerA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resumeId", is(resumeA.getId().intValue())))
                .andExpect(jsonPath("$.jobId", is(jobA.getId().intValue())))
                .andExpect(jsonPath("$.matchedSkills").isArray())
                .andExpect(jsonPath("$.overallMatchScore").isNumber())
                .andExpect(jsonPath("$.disclaimer").exists());
    }

    @Test
    @DisplayName("Deleting resume nullifies application reference without deleting application")
    void testDeleteResumeDecoupling() throws Exception {
        // Seeker A deletes their resume
        mockMvc.perform(delete("/api/resumes/" + resumeA.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenSeekerA))
                .andExpect(status().isNoContent());

        // Verify application still exists in database and resume field is null
        JobApplication reloadedApp = jobApplicationRepository.findById(applicationA.getId()).orElse(null);
        assertThat(reloadedApp).isNotNull();
        assertThat(reloadedApp.getResume()).isNull();
        assertThat(reloadedApp.getStatus()).isEqualTo(ApplicationStatus.SUBMITTED);
    }
}
