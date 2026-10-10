package com.hirewise.hirewise_backend.security;

import com.hirewise.hirewise_backend.entity.ApplicationStatus;
import com.hirewise.hirewise_backend.entity.EmploymentType;
import com.hirewise.hirewise_backend.entity.Job;
import com.hirewise.hirewise_backend.entity.JobApplication;
import com.hirewise.hirewise_backend.entity.JobStatus;
import com.hirewise.hirewise_backend.entity.Role;
import com.hirewise.hirewise_backend.entity.User;
import com.hirewise.hirewise_backend.repository.JobApplicationRepository;
import com.hirewise.hirewise_backend.repository.JobRepository;
import com.hirewise.hirewise_backend.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@TestPropertySource(properties = {
    "hirewise.jwt.secret=TestSecretKeyForHireWiseAppTestingMustBeAtLeast32BytesLong!"
})
class JobApplicationSecurityIntegrationTest {

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

    private MockMvc mockMvc;

    private User recruiterA;
    private User recruiterB;
    private User seekerA;
    private User seekerB;

    private String tokenRecruiterA;
    private String tokenRecruiterB;
    private String tokenSeekerA;
    private String tokenSeekerB;

    private Job publishedJob;
    private Job draftJob;

    private final List<Long> createdApplicationIds = new ArrayList<>();
    private final List<Long> createdJobIds = new ArrayList<>();
    private final List<Long> createdUserIds = new ArrayList<>();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        recruiterA = getOrCreateTestUser("test_app_recruiter_a@integration.test", "Recruiter A", Role.RECRUITER);
        recruiterB = getOrCreateTestUser("test_app_recruiter_b@integration.test", "Recruiter B", Role.RECRUITER);
        seekerA = getOrCreateTestUser("test_app_seeker_a@integration.test", "Seeker A", Role.JOB_SEEKER);
        seekerB = getOrCreateTestUser("test_app_seeker_b@integration.test", "Seeker B", Role.JOB_SEEKER);

        tokenRecruiterA = jwtTokenProvider.generateToken(recruiterA);
        tokenRecruiterB = jwtTokenProvider.generateToken(recruiterB);
        tokenSeekerA = jwtTokenProvider.generateToken(seekerA);
        tokenSeekerB = jwtTokenProvider.generateToken(seekerB);

        Job pub = new Job();
        pub.setTitle("Full Stack Cloud Lead");
        pub.setDescription("Architecture and full stack engineering");
        pub.setLocation("Denver, CO");
        pub.setEmploymentType(EmploymentType.FULL_TIME);
        pub.setSalaryRange("$170,000");
        pub.setRequiredSkills(Set.of("Java", "TypeScript", "AWS"));
        pub.setStatus(JobStatus.PUBLISHED);
        pub.setRecruiter(recruiterA);
        publishedJob = jobRepository.save(pub);
        createdJobIds.add(publishedJob.getId());

        Job draft = new Job();
        draft.setTitle("Draft Cloud Position");
        draft.setDescription("Unpublished job description");
        draft.setLocation("Austin, TX");
        draft.setEmploymentType(EmploymentType.FULL_TIME);
        draft.setRequiredSkills(Set.of("Java"));
        draft.setStatus(JobStatus.DRAFT);
        draft.setRecruiter(recruiterA);
        draftJob = jobRepository.save(draft);
        createdJobIds.add(draftJob.getId());
    }

    private User getOrCreateTestUser(String email, String name, Role role) {
        return userRepository.findByEmail(email).orElseGet(() -> {
            User u = new User(name, email, "hashed_password", role);
            User saved = userRepository.save(u);
            createdUserIds.add(saved.getId());
            return saved;
        });
    }

    @AfterEach
    void tearDown() {
        for (Long appId : createdApplicationIds) {
            try {
                jobApplicationRepository.deleteById(appId);
            } catch (Exception ignored) {
            }
        }
        for (Long jobId : createdJobIds) {
            try {
                jobApplicationRepository.findByJobId(jobId, org.springframework.data.domain.Pageable.unpaged())
                        .forEach(app -> jobApplicationRepository.delete(app));
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

    @Test
    @DisplayName("POST /api/jobs/{id}/applications requires authentication (401)")
    void applyToJob_MissingToken_Returns401() throws Exception {
        mockMvc.perform(post("/api/jobs/" + publishedJob.getId() + "/applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"coverLetter\":\"Hello\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/jobs/{id}/applications forbidden for RECRUITER (403)")
    void applyToJob_RecruiterRole_Returns403() throws Exception {
        mockMvc.perform(post("/api/jobs/" + publishedJob.getId() + "/applications")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenRecruiterA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"coverLetter\":\"Hello\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /api/jobs/{id}/applications fails on DRAFT job (400)")
    void applyToJob_DraftJob_Returns400() throws Exception {
        mockMvc.perform(post("/api/jobs/" + draftJob.getId() + "/applications")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenSeekerA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"coverLetter\":\"Hello\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("Applications are only accepted for published jobs")));
    }

    @Test
    @DisplayName("Job seeker successfully applies and duplicate application returns 409 Conflict")
    void applyToJob_Success_And_DuplicateConflict() throws Exception {
        String payload = "{\"coverLetter\":\"Extensive 8 years Java experience\"}";

        // First application -> 201 Created
        String responseBody = mockMvc.perform(post("/api/jobs/" + publishedJob.getId() + "/applications")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenSeekerA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status", is("SUBMITTED")))
                .andExpect(jsonPath("$.applicant.email", is("test_app_seeker_a@integration.test")))
                .andReturn().getResponse().getContentAsString();

        if (responseBody.contains("\"id\":")) {
            String idStr = responseBody.split("\"id\":")[1].split(",")[0].trim();
            createdApplicationIds.add(Long.parseLong(idStr));
        }

        // Duplicate application -> 409 Conflict
        mockMvc.perform(post("/api/jobs/" + publishedJob.getId() + "/applications")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenSeekerA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error", is("Conflict")))
                .andExpect(jsonPath("$.message", is("You have already applied for this job")));
    }

    @Test
    @DisplayName("Recruiter A can view applications for their job, but Recruiter B cannot (403)")
    void recruiterApplicationsAccess_OwnershipEnforced() throws Exception {
        // Seeker A applies
        JobApplication app = new JobApplication(publishedJob, seekerA, "Cover letter");
        JobApplication saved = jobApplicationRepository.save(app);
        createdApplicationIds.add(saved.getId());

        // Recruiter A owns publishedJob -> 200 OK
        mockMvc.perform(get("/api/jobs/" + publishedJob.getId() + "/applications")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenRecruiterA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].applicant.email", is("test_app_seeker_a@integration.test")));

        // Recruiter B does not own publishedJob -> 403 Forbidden
        mockMvc.perform(get("/api/jobs/" + publishedJob.getId() + "/applications")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenRecruiterB))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Recruiter advances application status, candidate withdraws, and further recruiter updates are rejected")
    void fullApplicationLifecycle_TransitionsAndWithdrawal() throws Exception {
        JobApplication app = new JobApplication(publishedJob, seekerA, "Candidate note");
        JobApplication saved = jobApplicationRepository.save(app);
        createdApplicationIds.add(saved.getId());

        // Recruiter advances to SHORTLISTED -> 200 OK
        mockMvc.perform(patch("/api/applications/" + saved.getId() + "/status")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenRecruiterA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"SHORTLISTED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("SHORTLISTED")));

        // Different seeker B tries to withdraw Seeker A's application -> 403 Forbidden
        mockMvc.perform(patch("/api/applications/" + saved.getId() + "/withdraw")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenSeekerB))
                .andExpect(status().isForbidden());

        // Seeker A withdraws own application -> 200 OK
        mockMvc.perform(patch("/api/applications/" + saved.getId() + "/withdraw")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenSeekerA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("WITHDRAWN")));

        // Recruiter tries to update withdrawn application -> 400 Bad Request
        mockMvc.perform(patch("/api/applications/" + saved.getId() + "/status")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenRecruiterA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"INTERVIEW\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("Cannot update status of a withdrawn application")));
    }
}
