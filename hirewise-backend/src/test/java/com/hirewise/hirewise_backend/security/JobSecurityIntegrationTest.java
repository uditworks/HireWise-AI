package com.hirewise.hirewise_backend.security;

import com.hirewise.hirewise_backend.dto.JobCreateRequest;
import com.hirewise.hirewise_backend.entity.EmploymentType;
import com.hirewise.hirewise_backend.entity.Job;
import com.hirewise.hirewise_backend.entity.JobStatus;
import com.hirewise.hirewise_backend.entity.Role;
import com.hirewise.hirewise_backend.entity.User;
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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@TestPropertySource(properties = {
    "hirewise.jwt.secret=TestSecretKeyForHireWiseAppTestingMustBeAtLeast32BytesLong!"
})
class JobSecurityIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JobRepository jobRepository;

    private MockMvc mockMvc;

    private User recruiterA;
    private User recruiterB;
    private User seeker;

    private String tokenRecruiterA;
    private String tokenRecruiterB;
    private String tokenSeeker;

    private Job testPublishedJob;
    private Job testDraftJob;

    private final List<Long> createdJobIds = new ArrayList<>();
    private final List<Long> createdUserIds = new ArrayList<>();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        // Use or create isolated test accounts
        recruiterA = getOrCreateTestUser("test_recruiter_a@integration.test", "Recruiter A", Role.RECRUITER);
        recruiterB = getOrCreateTestUser("test_recruiter_b@integration.test", "Recruiter B", Role.RECRUITER);
        seeker = getOrCreateTestUser("test_seeker_c@integration.test", "Seeker C", Role.JOB_SEEKER);

        tokenRecruiterA = jwtTokenProvider.generateToken(recruiterA);
        tokenRecruiterB = jwtTokenProvider.generateToken(recruiterB);
        tokenSeeker = jwtTokenProvider.generateToken(seeker);

        Job pub = new Job();
        pub.setTitle("Integration Published Job");
        pub.setDescription("Public description for searching and viewing");
        pub.setLocation("Chicago, IL");
        pub.setEmploymentType(EmploymentType.FULL_TIME);
        pub.setSalaryRange("$130,000");
        pub.setRequiredSkills(Set.of("Java", "PostgreSQL"));
        pub.setStatus(JobStatus.PUBLISHED);
        pub.setRecruiter(recruiterA);
        testPublishedJob = jobRepository.save(pub);
        createdJobIds.add(testPublishedJob.getId());

        Job draft = new Job();
        draft.setTitle("Integration Draft Job");
        draft.setDescription("Draft description not visible to public");
        draft.setLocation("Austin, TX");
        draft.setEmploymentType(EmploymentType.CONTRACT);
        draft.setSalaryRange("$90/hr");
        draft.setRequiredSkills(Set.of("Docker", "AWS"));
        draft.setStatus(JobStatus.DRAFT);
        draft.setRecruiter(recruiterA);
        testDraftJob = jobRepository.save(draft);
        createdJobIds.add(testDraftJob.getId());
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

    @Test
    @DisplayName("GET /api/jobs is public and returns only PUBLISHED jobs with comprehensive filters")
    void getPublishedJobs_PublicAccess_Returns200() throws Exception {
        // 1. Keyword matching title
        mockMvc.perform(get("/api/jobs")
                        .param("keyword", "Integration Published Job"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title", is("Integration Published Job")));

        // 2. Keyword matching required skill
        mockMvc.perform(get("/api/jobs")
                        .param("keyword", "PostgreSQL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title", is("Integration Published Job")));

        // 3. Location filter
        mockMvc.perform(get("/api/jobs")
                        .param("location", "Chicago"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title", is("Integration Published Job")));

        // 4. Employment type filter (matching published job)
        mockMvc.perform(get("/api/jobs")
                        .param("employmentType", "FULL_TIME"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title", is("Integration Published Job")));

        // 5. Employment type filter (no published job matching CONTRACT)
        mockMvc.perform(get("/api/jobs")
                        .param("employmentType", "CONTRACT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()", is(0)));

        // 6. Combined filters (keyword + location + employmentType)
        mockMvc.perform(get("/api/jobs")
                        .param("keyword", "Java")
                        .param("location", "Chicago")
                        .param("employmentType", "FULL_TIME"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title", is("Integration Published Job")));

        // 7. Verify draft job is NOT returned in public search
        mockMvc.perform(get("/api/jobs")
                        .param("keyword", "Integration Draft Job"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()", is(0)));
    }

    @Test
    @DisplayName("GET /api/jobs/{id} returns 200 for PUBLISHED job without authentication")
    void getJobById_Published_PublicAccess_Returns200() throws Exception {
        mockMvc.perform(get("/api/jobs/" + testPublishedJob.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("Integration Published Job")))
                .andExpect(jsonPath("$.status", is("PUBLISHED")));
    }

    @Test
    @DisplayName("GET /api/jobs/{id} returns 404 for DRAFT job when accessed publicly or by non-owner")
    void getJobById_Draft_PublicAccess_Returns404() throws Exception {
        // Public anonymous
        mockMvc.perform(get("/api/jobs/" + testDraftJob.getId()))
                .andExpect(status().isNotFound());

        // Authenticated Job Seeker
        mockMvc.perform(get("/api/jobs/" + testDraftJob.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenSeeker))
                .andExpect(status().isNotFound());

        // Authenticated different Recruiter
        mockMvc.perform(get("/api/jobs/" + testDraftJob.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenRecruiterB))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/jobs/{id} returns 200 for DRAFT job when accessed by the owning recruiter")
    void getJobById_Draft_Owner_Returns200() throws Exception {
        mockMvc.perform(get("/api/jobs/" + testDraftJob.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenRecruiterA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("Integration Draft Job")))
                .andExpect(jsonPath("$.status", is("DRAFT")));
    }

    @Test
    @DisplayName("POST /api/jobs returns 401 Unauthorized when missing token")
    void createJob_MissingToken_Returns401() throws Exception {
        String payload = """
                {
                    "title": "Unauthorized Job",
                    "description": "Valid job description for testing unauthorized status",
                    "location": "Remote",
                    "employmentType": "FULL_TIME",
                    "requiredSkills": ["Java"]
                }
                """;

        mockMvc.perform(post("/api/jobs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/jobs returns 403 Forbidden when caller has ROLE_JOB_SEEKER")
    void createJob_JobSeeker_Returns403() throws Exception {
        String payload = """
                {
                    "title": "Seeker Attempted Job",
                    "description": "Valid job description for testing forbidden access",
                    "location": "Remote",
                    "employmentType": "FULL_TIME",
                    "requiredSkills": ["Java"]
                }
                """;

        mockMvc.perform(post("/api/jobs")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenSeeker)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error", is("Forbidden")));
    }

    @Test
    @DisplayName("POST /api/jobs returns 201 Created when caller has ROLE_RECRUITER")
    void createJob_Recruiter_Returns201() throws Exception {
        String payload = """
                {
                    "title": "Recruiter Created Job",
                    "description": "Valid job description for testing recruiter job creation",
                    "location": "Seattle, WA",
                    "employmentType": "FULL_TIME",
                    "salaryRange": "$150,000 - $180,000",
                    "requiredSkills": ["Java", "Spring Boot", "MySQL"],
                    "publishImmediately": true
                }
                """;

        String responseBody = mockMvc.perform(post("/api/jobs")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenRecruiterA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title", is("Recruiter Created Job")))
                .andExpect(jsonPath("$.status", is("PUBLISHED")))
                .andExpect(jsonPath("$.recruiter.email", is("test_recruiter_a@integration.test")))
                .andReturn().getResponse().getContentAsString();

        // Extract ID for cleanup
        if (responseBody.contains("\"id\":")) {
            String idStr = responseBody.split("\"id\":")[1].split(",")[0].trim();
            createdJobIds.add(Long.parseLong(idStr));
        }
    }

    @Test
    @DisplayName("GET /api/jobs/my-jobs returns 401 without token and 403 for JOB_SEEKER")
    void getMyJobs_SecurityChecks() throws Exception {
        mockMvc.perform(get("/api/jobs/my-jobs"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/jobs/my-jobs")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenSeeker))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/jobs/my-jobs")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenRecruiterA))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /api/jobs/{id} returns 403 Forbidden when a different recruiter attempts update")
    void updateJob_DifferentRecruiter_Returns403() throws Exception {
        String updatePayload = """
                {
                    "title": "Tampered Job Title",
                    "description": "Tampered job description attempting to overwrite",
                    "location": "Nowhere",
                    "employmentType": "CONTRACT",
                    "requiredSkills": ["Hacking"]
                }
                """;

        // Recruiter B tries to update Recruiter A's job
        mockMvc.perform(put("/api/jobs/" + testPublishedJob.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenRecruiterB)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatePayload))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("DELETE /api/jobs/{id} returns 403 Forbidden when a different recruiter attempts delete")
    void deleteJob_DifferentRecruiter_Returns403() throws Exception {
        mockMvc.perform(delete("/api/jobs/" + testPublishedJob.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenRecruiterB))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PUT /api/jobs/{id} returns 200 OK when owner updates their job")
    void updateJob_Owner_Returns200() throws Exception {
        String updatePayload = """
                {
                    "title": "Owner Updated Title",
                    "description": "Legitimate updated description by the owning recruiter",
                    "location": "Boston, MA",
                    "employmentType": "FULL_TIME",
                    "salaryRange": "$160,000",
                    "requiredSkills": ["Java", "Cloud"]
                }
                """;

        mockMvc.perform(put("/api/jobs/" + testPublishedJob.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenRecruiterA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatePayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("Owner Updated Title")))
                .andExpect(jsonPath("$.location", is("Boston, MA")));
    }
}
