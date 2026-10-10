package com.hirewise.hirewise_backend.controller;

import com.hirewise.hirewise_backend.dto.ApplicantSummaryResponse;
import com.hirewise.hirewise_backend.dto.ApplicationStatusUpdateRequest;
import com.hirewise.hirewise_backend.dto.JobApplicationCreateRequest;
import com.hirewise.hirewise_backend.dto.JobApplicationResponse;
import com.hirewise.hirewise_backend.dto.JobSummaryResponse;
import com.hirewise.hirewise_backend.entity.ApplicationStatus;
import com.hirewise.hirewise_backend.entity.EmploymentType;
import com.hirewise.hirewise_backend.entity.JobStatus;
import com.hirewise.hirewise_backend.exception.GlobalExceptionHandler;
import com.hirewise.hirewise_backend.service.JobApplicationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class JobApplicationControllerTest {

    private MockMvc mockMvc;

    @Mock
    private JobApplicationService jobApplicationService;

    @InjectMocks
    private JobApplicationController jobApplicationController;

    private JobApplicationResponse sampleResponse;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(jobApplicationController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .setValidator(validator)
                .build();

        JobSummaryResponse job = new JobSummaryResponse(10L, "Java Architect", "Remote",
                EmploymentType.FULL_TIME, JobStatus.PUBLISHED);
        ApplicantSummaryResponse applicant = new ApplicantSummaryResponse(1L, "Alice", "alice@example.com");
        sampleResponse = new JobApplicationResponse(100L, job, applicant,
                ApplicationStatus.SUBMITTED, "Hello world", LocalDateTime.now(), LocalDateTime.now());
    }

    @Test
    @DisplayName("POST /api/jobs/{jobId}/applications returns 201 Created on valid submission")
    void applyToJob_ValidInput_Returns201() throws Exception {
        String payload = """
                {
                    "coverLetter": "Detailed candidate background and qualifications"
                }
                """;

        when(jobApplicationService.applyToJob(eq(10L), any(), eq("alice@example.com")))
                .thenReturn(sampleResponse);

        mockMvc.perform(post("/api/jobs/10/applications")
                        .principal(new UsernamePasswordAuthenticationToken("alice@example.com", null))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(100)))
                .andExpect(jsonPath("$.status", is("SUBMITTED")))
                .andExpect(jsonPath("$.applicant.email", is("alice@example.com")));
    }

    @Test
    @DisplayName("GET /api/applications/my-applications returns 200 OK with paginated list")
    void getMyApplications_Returns200() throws Exception {
        when(jobApplicationService.getMyApplications(eq("alice@example.com"), any()))
                .thenReturn(new PageImpl<>(List.of(sampleResponse), PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/api/applications/my-applications")
                        .principal(new UsernamePasswordAuthenticationToken("alice@example.com", null))
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id", is(100)));
    }

    @Test
    @DisplayName("PATCH /api/applications/{id}/status with missing status returns 400 Bad Request")
    void updateApplicationStatus_MissingStatus_Returns400() throws Exception {
        mockMvc.perform(patch("/api/applications/100/status")
                        .principal(new UsernamePasswordAuthenticationToken("jane@company.com", null))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Validation Failed")))
                .andExpect(jsonPath("$.details.status").exists());
    }

    @Test
    @DisplayName("PATCH /api/applications/{id}/withdraw returns 200 OK")
    void withdrawApplication_Returns200() throws Exception {
        sampleResponse.setStatus(ApplicationStatus.WITHDRAWN);
        when(jobApplicationService.withdrawApplication(100L, "alice@example.com"))
                .thenReturn(sampleResponse);

        mockMvc.perform(patch("/api/applications/100/withdraw")
                        .principal(new UsernamePasswordAuthenticationToken("alice@example.com", null)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("WITHDRAWN")));
    }
}
